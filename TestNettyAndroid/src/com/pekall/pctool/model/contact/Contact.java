
package com.pekall.pctool.model.contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Contact {

    /*
     * #列举出联系人模块可供操作的所有属性姓-------String Xing;名-------String Ming;
     * 电话号码(不同电话类型)-------List<Map<String, String>> Number
     * 电子邮件(不同的邮件类型)-------List<Map<String, String>> Email
     * 通讯工具(不同的交流工具)------List<Map<String, String>> Exchange
     * 通讯地址(
     * 不同的地方通讯地址)----------List<Map<String, AddressInfo>> Address;
     * Address组织(包括公司，职位等其他内容)--------List<Map<String, String>> Organization
     * Organization备注(包括详细描述信息)----------------String Comment
     */

    public int _ID;
    public String Name;
    public String familyName;
    public String givenName;
    public List<Map<String, String>> Number=new ArrayList<Map<String,String>>();
    public List<Map<String, String>> Email=new ArrayList<Map<String,String>>();
    public List<Map<String, String>> Exchange=new ArrayList<Map<String,String>>();
    public List<Map<String, AddressInfo>> Address=new ArrayList<Map<String,AddressInfo>>();
    public List<Map<String, String>> Organization=new ArrayList<Map<String,String>>();
    public String Comment;
}

