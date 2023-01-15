package com.example.vertx.verticle.launcher;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * 환경 설정 관련
 */
public class LauncherConfig
{
	/**
	 * 웹 서버 환경 설정 관련
	 */
	private Web web = new Web();
	
	/**
	 * 웹 서버 환경 설정 관련
	 */
	public static class Web
	{
		public Integer port;
		public Boolean logActivity;
		public String adminUserName;
		public String adminPassword;
		public Long sessionTimeout;
		public String auzureADClientID;
		public String auzureADTenantID;
		public String auzureADClientKey;
		public String auzureADClientSecret;
		public String auzureADLoginRedirectURL;
		public Long TOTPTimeStep;
		public Integer TOTPMovingFactorOffset;
		
		/**
		 * 웹 서버 포트
		 */
		public static Integer getWebPort() { return CONFIG.web.port; }
		/**
		 * 웹 서버 로그 활성화 여부
		 */
		public static Boolean getWebLogActivity() { return CONFIG.web.logActivity; }
		/**
		 * 웹 서버 admin 계정 ID
		 */
		public static String getAdminUserName() { return CONFIG.web.adminUserName; }
		/**
		 * 웹 서버 admin 계정 PW
		 */
		public static String getAdminPassword() { return CONFIG.web.adminPassword; }
		/**
		 * 세션 만료 시간(단위:ms)
		 */
		public static Long getSessionTimeout() { return CONFIG.web.sessionTimeout; }
		/**
		 * Azure AD 클라이언트 ID
		 */
		public static String getAuzureADClientID() { return CONFIG.web.auzureADClientID; }
		/**
		 * Azure AD 테넌트 ID
		 */
		public static String getAuzureADTenantID() { return CONFIG.web.auzureADTenantID; }
		/**
		 * Azure AD 클라이언트 키
		 */
		public static String getAuzureADClientKey() { return CONFIG.web.auzureADClientKey; }
		/**
		 * Azure AD 클라이언트 시크릿
		 */
		public static String getAuzureADClientSecret() { return CONFIG.web.auzureADClientSecret; }
		/**
		 * Azure AD 로그인 콜백 URL
		 */
		public static String getAuzureADLoginRedirectURL() { return CONFIG.web.auzureADLoginRedirectURL; }
		/**
		 * TOTP 갱신 시간 간격(단위:Ticks, 디폴트:30초=3000L*10L)
		 */
		public static Long getTOTPTimeStep() { return CONFIG.web.TOTPTimeStep; }
		/**
		 * TOTP 무빙 팩터 오프셋
		 */
		public static Integer getTOTPMovingFactorOffset() { return CONFIG.web.TOTPMovingFactorOffset; }
	}
	
	private static final String FILE_NAME = "config.xml";
	private static final LauncherConfig CONFIG = new LauncherConfig();
	
	private LauncherConfig()
	{
		try {
			URL url_path = ClassLoader.getSystemResource(FILE_NAME);
			String file_path = (null == url_path) ? null : url_path.getPath();
			if (null == file_path)
				throw new NullPointerException();
			File file = new File(file_path);
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.parse(file);
			doc.getDocumentElement().normalize();
			String root_name = doc.getDocumentElement().getNodeName();
			NodeList node_list = doc.getElementsByTagName(root_name);
			if (0 >= node_list.getLength())
				throw new NullPointerException();
			Node node = node_list.item(0);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				for (Field fd : web.getClass().getFields()) {
					String fn = fd.getName();
					if (0 >= element.getElementsByTagName(fn).getLength())
						continue;
					NodeList child_nodes = element.getElementsByTagName(fn).item(0).getChildNodes();
					if (0 >= child_nodes.getLength())
						continue;
					Node end_node = child_nodes.item(0);
					String fv = end_node.getNodeValue();
					if (null == fv)
						continue;
					Class<?> type = fd.getType();
					if (type.equals(String.class)) {
						fd.set(web, fv);
					} else if (type.equals(int.class) || type.equals(Integer.class)) {
						fd.set(web, Integer.valueOf(fv));
					} else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
						fd.set(web, Boolean.valueOf(fv));
					} else if (type.equals(long.class) || type.equals(Long.class)) {
						fd.set(web, Long.valueOf(fv));
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
