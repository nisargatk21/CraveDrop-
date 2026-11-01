package com.cravedrop.service;
import com.cravedrop.dao.*; import com.cravedrop.model.Models.*; import java.sql.SQLException; import java.util.*;
public class OrderService {
 final MenuDAO menu=new MenuDAO(); final OrderDAO dao=new OrderDAO();
 public static final List<String> PAYMENTS=List.of("UPI (Google Pay / PhonePe)","Credit/Debit Card","Cash on Delivery");
 public static final double GST_RATE=0.05, DELIVERY_FEE=40.0, PLATFORM_FEE=8.0;
 public record PriceBreakdown(double subtotal,double gst,double deliveryFee,double platformFee,double grandTotal){}
 public List<CartLine> lines(Map<Integer,Integer> cart) throws SQLException {
  var m=menu.findByIds(cart.keySet()); var out=new ArrayList<CartLine>(); cart.forEach((id,q)->{var i=m.get(id);if(i!=null)out.add(new CartLine(i,q));}); return out;}
 private static double money(double value){return Math.round(value*100)/100.0;}
 public PriceBreakdown breakdown(List<CartLine> l){double subtotal=money(l.stream().mapToDouble(CartLine::total).sum());double gst=money(subtotal*GST_RATE);
  return new PriceBreakdown(subtotal,gst,DELIVERY_FEE,PLATFORM_FEE,money(subtotal+gst+DELIVERY_FEE+PLATFORM_FEE));}
 public double subtotal(List<CartLine> l){return breakdown(l).subtotal();}
 public double total(List<CartLine> l){return breakdown(l).grandTotal();}
 /** Totals are recomputed from DB prices, never trusted from the browser. */
 public int place(User u,Map<Integer,Integer> cart,String addr,String phone,String pay) throws SQLException {
  var l=lines(cart);
  if(l.isEmpty())throw new IllegalArgumentException("Your cart is empty.");
  if(l.stream().map(line->line.item().restaurant()).distinct().count()>1)throw new IllegalArgumentException("A single order can contain items from only one restaurant.");
  if(addr==null||addr.trim().length()<10)throw new IllegalArgumentException("Enter a full delivery address (at least 10 characters).");
  if(phone==null||!phone.matches("\\d{10}"))throw new IllegalArgumentException("Enter a 10-digit contact phone number.");
  if(!PAYMENTS.contains(pay))throw new IllegalArgumentException("Choose a payment method.");
  return dao.create(u.id(),total(l),pay,addr.trim(),phone,l);}
 public List<Order> history(int uid) throws SQLException {return dao.listByUser(uid);}
 public Order findForUser(int uid,int orderId) throws SQLException {return dao.findByUser(uid,orderId);}
}
