package com.cravedrop.model;
import java.sql.Timestamp; import java.util.List;
public class Models {
 public record User(int id,String name,String email,String phone,String role,String hash){
  public int getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
  public String getPhone(){return phone;} public String getRole(){return role;} public String getHash(){return hash;}
 }
 public record Item(int id,String restaurant,String category,String name,String description,double price,String image,boolean vegetarian){
  // JavaBean accessors let older Eclipse JSP validators inspect record values.
  public int getId(){return id;} public String getRestaurant(){return restaurant;} public String getCategory(){return category;}
  public String getName(){return name;} public String getDescription(){return description;} public double getPrice(){return price;}
  public String getImage(){return image;} public boolean isVegetarian(){return vegetarian;}
 }
 public record CartLine(Item item,int qty){
  public double total(){return item.price()*qty;}
  public Item getItem(){return item;} public int getQty(){return qty;} public double getTotal(){return total();}
 }
 public record Line(String name,int qty,double price){
  public String getName(){return name;} public int getQty(){return qty;} public double getPrice(){return price;}
 }
 public record Order(int id,int number,double total,String status,String payment,String address,Timestamp at,List<Line> lines){
  public int getId(){return id;} public int getNumber(){return number;} public double getTotal(){return total;}
  public String getStatus(){return status;} public String getPayment(){return payment;} public String getAddress(){return address;}
  public Timestamp getAt(){return at;} public List<Line> getLines(){return lines;}
 }
 public record AdminStats(int customers,int owners,int menuItems,int orders,int openOrders,double revenue){
  public int getCustomers(){return customers;} public int getOwners(){return owners;} public int getMenuItems(){return menuItems;}
  public int getOrders(){return orders;} public int getOpenOrders(){return openOrders;} public double getRevenue(){return revenue;}
 }
 public record AdminOrder(int id,String customer,String email,double total,String status,Timestamp at,int deliveryPartnerId,String deliveryPartner){
  public int getId(){return id;} public String getCustomer(){return customer;} public String getEmail(){return email;}
  public double getTotal(){return total;} public String getStatus(){return status;} public Timestamp getAt(){return at;}
  public int getDeliveryPartnerId(){return deliveryPartnerId;} public String getDeliveryPartner(){return deliveryPartner;}
 }
 public record AdminUser(int id,String name,String email,String phone,String role,Timestamp at){
  public int getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
  public String getPhone(){return phone;} public String getRole(){return role;} public Timestamp getAt(){return at;}
 }
 public record AdminDeliveryPartner(int id,String name,String phone,String area,String status){
  public int getId(){return id;} public String getName(){return name;} public String getPhone(){return phone;}
  public String getArea(){return area;} public String getStatus(){return status;}
 }
}
