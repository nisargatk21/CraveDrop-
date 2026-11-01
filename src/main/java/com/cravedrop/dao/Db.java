package com.cravedrop.dao;
import java.sql.*;
/** Only place that knows connection details. Override with DB_URL / DB_USER / DB_PASS env vars. */
public class Db {
 static String env(String k,String d){String v=System.getenv(k);return v==null?d:v;}
 public static Connection get() throws SQLException {
  try{Class.forName("com.mysql.cj.jdbc.Driver");}
  catch(ClassNotFoundException e){throw new SQLException("MySQL driver jar missing from WEB-INF/lib",e);}
  return DriverManager.getConnection(env("DB_URL","jdbc:mysql://localhost:3306/cravedrop"),env("DB_USER","root"),env("DB_PASS",""));}
 static void update(String sql,Object... a) throws SQLException {
  try(var c=get();var p=c.prepareStatement(sql)){for(int i=0;i<a.length;i++)p.setObject(i+1,a[i]);p.executeUpdate();}}
}