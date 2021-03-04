package cn.yan.util;

import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * 数据为null的返回处理
 * @author up
 *
 */
public class JuDataTypeUtils {
	
	public static String getString(String str){
		return str == null || StringUtils.endsWithIgnoreCase(str, "") ? null : str;
	}
	
	public static Integer getInt(Integer integer){
		return integer == null ? 0 : integer;
	}
	
	public static String getStringValue(Object obj){
		if (obj != null) {
			return obj + "";
		}
		return "";
	}
	
	public static boolean getStringFlag(String str){
		if (str != null && !"".equals(str)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 通过关键字查找对应的股交流程字典信息
	 * @param param
	 * @param search
	 * @return
	 */
	public static String getProcessKey(Map<String,String> param,String search){
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : param.entrySet()) {
			if (entry.getValue().contains(search)) {
				sb.append(entry.getKey()).append(",");
			}
		}
		String str = sb.toString();
		return str.substring(0, sb.length() - 1);
	}
	
	/**
	 * 字符串转数组
	 * @param str
	 * @return
	 */
	public static String [] getStrs(String str){
		String [] strs = {};
		// 如果不为空就切割
		if (!StringUtils.isEmpty(str)) {
			strs = str.split("\\,");
		} else {
			strs = null;
		}
		return strs;
	}
	
	/**
	 * 设置相应流程的任务状态
	 * @param param 股交返回的
	 * @param search
	 * @return
	 */
	public static String getTaskStatus(Map<String, String> param,String search){
		String proVal = param.get(search);
		String taskStatus = null;
		if (proVal.contains("[股东")) {
			taskStatus = "1";
		}else {
			taskStatus = "3";
		}
		return taskStatus;
	}
	
	public static void main(String[] args) {
		String string = "";
		String str = null;
		System.out.println(getString(string));
		System.out.println(getString(str));
		System.out.println(getStringFlag(null));
	}
}
