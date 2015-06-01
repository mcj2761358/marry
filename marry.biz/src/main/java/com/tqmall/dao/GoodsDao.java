package com.tqmall.dao;

import com.tqmall.model.Goods;
import com.tqmall.utils.JDBCUtils;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minutch on 2015-05-31.
 */
public class GoodsDao {

    private Connection conn = JDBCUtils.getConn();

    public List<Goods> getAllData() {
        List<Goods> goodsList = new ArrayList<Goods>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from goods";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Goods goods = new Goods();
                goods.setId(rs.getInt("id"));
                goods.setSn(rs.getString("sn"));
                goods.setName(rs.getString("name"));
                goods.setPrice(rs.getDouble("price"));
                goods.setUnit(rs.getString("unit"));
                goods.setCtime(rs.getString("ctime"));
                goods.setDes(rs.getString("des"));
                goodsList.add(goods);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            JDBCUtils.closeStmtAndRs(stmt,rs);
        }
        return goodsList;
    }
}
