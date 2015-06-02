package com.tqmall.index;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.tqmall.model.Goods;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 实时索引
 * Created by Minutch on 2015-06-02.
 */
public class GoodsRealTimeIndex {

    /**
     * Canal监控目标
     */
    public static final String DESTINATION = "goods";
    /**
     * 数据库名称
     */
    public static final String DB_NAME = "test";
    /**
     * canal服务器端口号
     */
    public static final Integer PORT = 11111;

    /**
     * 需要监控的Canal事件
     */
    public static Map<String, CanalEntry.EventType> canalEventMap = new HashMap<String, CanalEntry.EventType>();

    /**
     * 实时索引正在运行标记
     */
    private boolean running = false;
    /**
     * 监控Canal的线程
     */
    private Thread thread = null;
    /**
     * canal连接
     */
    private CanalConnector canalConnector;
    /**
     * 索引时间
     */
    private Long indexTimeStamp = 0L;

    static {
        canalEventMap.put("UPDATE", CanalEntry.EventType.UPDATE);
        canalEventMap.put("DELETE", CanalEntry.EventType.DELETE);
        canalEventMap.put("INSERT", CanalEntry.EventType.INSERT);
    }
    /**
     * 启动实时索引
     * @param indexTimeStamp
     */
    public void start(Long indexTimeStamp) {
        this.indexTimeStamp = indexTimeStamp;
        run();
    }

    /**
     * 实时索引执行方法
     */
    private void run () {
        canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),PORT),DESTINATION,"","");
        Assert.notNull(canalConnector,"连接失败!");
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    process();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        running = true;
    }


    /**
     * 处理
     */
    private void process() throws Exception{

        System.out.println("process:开始监听目标["+DESTINATION+","+DB_NAME+"]");

        //每次从binlog读取的行数
        int batchSize = 1000;
        //连接canal服务器
        canalConnector.connect();
        //增量数据订阅
        canalConnector.subscribe();

        while (running) {
            Message message = canalConnector.getWithoutAck(batchSize);
            long batchId = message.getId();
            int size = message.getEntries().size();
            if (batchId!=-1 || size>0) {
                System.out.println("process:取到"+size+"条binlog");
                dealEntries(message.getEntries());
            }
            canalConnector.ack(batchId);
            //每隔100毫秒取一次
            Thread.sleep(1000);
//            canalConnector.rollback(batchId); // 处理失败, 回滚数据
        }
    }

    /**
     * 处理CanalEntries
     */
    private void dealEntries(List<CanalEntry.Entry> entries) throws Exception{
        Map<String, List<CanalEntry.RowData>> dataMap = new HashMap<String, List<CanalEntry.RowData>>();
        List<String> keyList = new LinkedList<String>();

        for (CanalEntry.Entry entry : entries) {

            //如果是事务开启和关闭，直接跳过
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            // 1.判断数据库名是否正确
            // 2.校验数据变更时间是否在实时索引启动之后
            // 3.是否ROWDATA
            if (DB_NAME.equals(entry.getHeader().getSchemaName())
                    && entry.getHeader().getExecuteTime() > indexTimeStamp
                    && entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {

                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                String dbName = entry.getHeader().getSchemaName();
                String tableName = entry.getHeader().getTableName();
                CanalEntry.EventType eventType = rowChange.getEventType();

                System.out.println("dealEntries:数据库表["+dbName+"."+tableName+"]发生数据变化.");

                //如果是查询和DDL事件，直接跳过
                if (eventType == CanalEntry.EventType.QUERY || rowChange.getIsDdl()) {
                    continue;
                }

                //给变化的数据按数据库，表名，事件类型进行归类
                String key = dbName + " " + tableName + " " + eventType.toString();
                if (!keyList.contains(key)) {
                    keyList.add(key);
                }

                //将变化的数据分组存放到dataMap中
                List<CanalEntry.RowData> dataList = new LinkedList<CanalEntry.RowData>();
                if (dataMap.containsKey(key)) {
                    dataList = dataMap.get(key);
                } else {
                    dataMap.put(key, dataList);
                }
                dataList.addAll(rowChange.getRowDatasList());
            }
        }


        //处理变化的数据
        for (String key : keyList) {
            String[] keyBuf = key.split(" ");
            //包含需要处理的事件
            if (canalEventMap.containsKey(keyBuf[2])) {
                //重建索引
                goodsAction(dataMap.get(key),canalEventMap.get(keyBuf[2]));
                Thread.sleep(100);
            }
        }
    }

    /**
     *
     */
    public void goodsAction(List<CanalEntry.RowData> dataList,CanalEntry.EventType eventType) {
        List<Integer> removeIdList = new LinkedList<Integer>();
        List<Goods> goodsList = new LinkedList<Goods>();
        for (CanalEntry.RowData rowData : dataList) {
            Map<String, CanalEntry.Column> beforeFieldsMap = makeFieldMap(rowData.getBeforeColumnsList());
            Map<String, CanalEntry.Column> afterFieldsMap = makeFieldMap(rowData.getAfterColumnsList());

            //删除和更新事件，都需要先删除原有索引
            if (eventType == CanalEntry.EventType.DELETE || eventType == CanalEntry.EventType.UPDATE) {
                String id = beforeFieldsMap.get("id").getValue();
                removeIdList.add(Integer.parseInt(id));
            }
            //插入和更新，都插入新索引
            if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                try {
                    // 更新索引
                    Goods goods = new Goods();
                    String id = afterFieldsMap.get("id").getValue();
                    String sn = afterFieldsMap.get("sn").getValue();
                    String name = afterFieldsMap.get("name").getValue();
                    String price = afterFieldsMap.get("price").getValue();
                    String unit = afterFieldsMap.get("unit").getValue();
                    String ctime = afterFieldsMap.get("ctime").getValue();
                    String des = afterFieldsMap.get("des").getValue();

                    if (id != null) {
                        goods.setId(Integer.valueOf(id));
                    }
                    goods.setSn(sn);
                    goods.setName(name);
                    if (price != null) {
                        goods.setPrice(Double.valueOf(price));
                    }
                    goods.setUnit(unit);
                    goods.setCtime(ctime);
                    goods.setDes(des);
                    goodsList.add(goods);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        GoodsFullIndex goodsFullIndex = new GoodsFullIndex();
        goodsFullIndex.bulkDeleteIndex(removeIdList);
        goodsFullIndex.bulkCreateIndex(goodsList);
    }

    /**
     *
     * @param columnsList
     * @return
     */
    protected Map<String, CanalEntry.Column> makeFieldMap(List<CanalEntry.Column> columnsList) {
        Map<String, CanalEntry.Column> fieldsMap = new HashMap<String, CanalEntry.Column>();
        for (CanalEntry.Column column : columnsList) {
            fieldsMap.put(column.getName(), column);
            column.getValue();
        }
        return fieldsMap;
    }
}













