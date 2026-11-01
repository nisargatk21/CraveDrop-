package com.cravedrop.service;
import com.cravedrop.dao.UserDAO; import com.cravedrop.model.Models.User;
import jakarta.mail.*; import jakarta.mail.internet.*; import org.mindrot.jbcrypt.BCrypt; import java.sql.SQLException; import java.util.*;
public class AuthService {
 final UserDAO dao=new UserDAO();
 static String pwErr(String p){return p==null||p.length()<8||!p.matches(".*\\d.*")||!p.matches(".*[A-Za-z].*")?"Password must be at least 8 characters with letters and a number.":null;}
 static boolean ok(String s){return s!=null&&!s.isBlank();}
 public String register(String n,String e,String ph,String pw,String role) throws SQLException {
  if(!ok(n))return "Name is required.";
  if(!ok(e)||!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))return "Enter a valid email address.";
  if(ph==null||!ph.matches("\\d{10}"))return "Enter a 10-digit phone number.";
  if(pwErr(pw)!=null)return pwErr(pw);
  e=e.trim().toLowerCase(); if(dao.findByEmail(e)!=null)return "That email is already registered. Please sign in.";
  dao.create(n.trim(),e,ph,BCrypt.hashpw(pw,BCrypt.gensalt()),"owner".equals(role)?"owner":"customer");return null;}
 public User login(String e,String pw) throws SQLException {
  if(!ok(e)||!ok(pw))return null; User u=dao.findByEmail(e.trim().toLowerCase()); return u!=null&&BCrypt.checkpw(pw,u.hash())?u:null;}
 public User findById(int id) throws SQLException {return dao.findById(id);}
 public String change(User u,String cur,String nw,String conf) throws SQLException {
  if(cur==null||!BCrypt.checkpw(cur,u.hash()))return "Current password is incorrect.";
  if(pwErr(nw)!=null)return pwErr(nw); if(!nw.equals(conf))return "New passwords do not match.";
  dao.updateHash(u.id(),BCrypt.hashpw(nw,BCrypt.gensalt()));return null;}
 public void startReset(String e,String base) throws SQLException {
  User u=ok(e)?dao.findByEmail(e.trim().toLowerCase()):null; if(u==null)return; // same response either way: no account enumeration
  String t=UUID.randomUUID().toString().replace("-",""); dao.setToken(u.id(),t); mail(u.email(),base+"/app/reset?token="+t);}
 public String reset(String t,String pw,String conf) throws SQLException {
  User u=ok(t)?dao.findByToken(t):null; if(u==null)return "This reset link is invalid or has expired.";
  if(pwErr(pw)!=null)return pwErr(pw); if(!pw.equals(conf))return "Passwords do not match.";
  dao.updateHash(u.id(),BCrypt.hashpw(pw,BCrypt.gensalt()));return null;}
 /** JavaMail via SMTP_HOST/PORT/USER/PASS env vars. Without SMTP_HOST the link is printed to the Tomcat log (dev mode). */
 void mail(String to,String link){
  String h=System.getenv("SMTP_HOST"); if(h==null){System.out.println("[DEV] Password reset link for "+to+": "+link);return;}
  try{var p=new Properties();p.put("mail.smtp.host",h);p.put("mail.smtp.port",Optional.ofNullable(System.getenv("SMTP_PORT")).orElse("587"));p.put("mail.smtp.auth","true");p.put("mail.smtp.starttls.enable","true");
   var s=Session.getInstance(p,new Authenticator(){protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(System.getenv("SMTP_USER"),System.getenv("SMTP_PASS"));}});
   var m=new MimeMessage(s);m.setFrom(new InternetAddress(System.getenv("SMTP_USER")));m.setRecipients(Message.RecipientType.TO,to);m.setSubject("CraveDrop password reset");
   m.setText("Reset your CraveDrop password (valid for 1 hour): "+link);Transport.send(m);}catch(Exception x){x.printStackTrace();}}
}
