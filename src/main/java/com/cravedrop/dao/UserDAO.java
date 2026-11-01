package com.cravedrop.dao;
import com.cravedrop.model.Models.User; import java.sql.*;
public class UserDAO {
 static User map(ResultSet r) throws SQLException {return new User(r.getInt("id"),r.getString("name"),r.getString("email"),r.getString("phone"),r.getString("role"),r.getString("password_hash"));}
 User one(String sql,String a) throws SQLException {try(var c=Db.get();var p=c.prepareStatement(sql)){p.setString(1,a);try(var r=p.executeQuery()){return r.next()?map(r):null;}}}
 public User findByEmail(String e) throws SQLException {return one("SELECT * FROM users WHERE email=?",e);}
 public User findById(int id) throws SQLException {try(var c=Db.get();var p=c.prepareStatement("SELECT * FROM users WHERE id=?")){p.setInt(1,id);try(var r=p.executeQuery()){return r.next()?map(r):null;}}}
 public User findByToken(String t) throws SQLException {return one("SELECT * FROM users WHERE reset_token=? AND reset_expiry>NOW()",t);}
 public void create(String n,String e,String ph,String hash,String role) throws SQLException {Db.update("INSERT INTO users(name,email,phone,password_hash,role) VALUES(?,?,?,?,?)",n,e,ph,hash,role);}
 public void updateHash(int id,String h) throws SQLException {Db.update("UPDATE users SET password_hash=?,reset_token=NULL,reset_expiry=NULL WHERE id=?",h,id);}
 public void setToken(int id,String t) throws SQLException {Db.update("UPDATE users SET reset_token=?,reset_expiry=DATE_ADD(NOW(),INTERVAL 1 HOUR) WHERE id=?",t,id);}
}
