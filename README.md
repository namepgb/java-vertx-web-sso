# java-vertx-web-sso
<p>
이 샘플은 OAuth 2.0(Open Authorization 2.0)과 MSAL4J(Microsoft Authentication Library for Java)를 사용한 Java 웹 서버에서의 SSO(Single Sign-On) 인증 방식을 구현합니다. 
이 샘플을 실행하려면 다음이 필요합니다.
</p>
<ul>
  <li>JDK 또는 OpenJDK</li>
  <li>Azure Active Directory(Azure AD) Tenant</li>
</ul>
<p>
  Azure AD Tenant 생성은 <a href="https://learn.microsoft.com/en-us/azure/active-directory/develop/quickstart-create-new-tenant">Microsoft Documentation(Quickstart: Set up tenant)</a>
  를 참고합니다.
  Azure AD Tenant를 생성하고 다음 정보를 획득합니다. 키 값 및 비밀 ID는 Tenant 생성 과정에서 한 번만 노출되므로 미리 기록해야 합니다.
</p>
<ul>
  <li>클라이언트 ID</li>
  <li>테넌트 ID</li>
  <li>클라이언트 키</li>
  <li>클라이언트 시크릿</li>
  <li>웹 리디렉션 URL</li>
</ul>
<h2>
  Sample overview
</h2>
<p>
  build.gradle에서 다음 의존성을 포함합니다.
</p>

```gradle
dependencies {
    implementation group: 'io.vertx', name: 'vertx-core', version: "$version_vertx"
    implementation group: 'io.vertx', name: 'vertx-web', version: "$version_vertx"
    implementation group: 'io.vertx', name: 'vertx-web-templ-jade', version: "$version_vertx"
    implementation group: 'io.vertx', name: 'vertx-auth-oauth2', version: "$version_vertx"
    implementation group: 'com.microsoft.azure', name: 'msal4j', version: "$version_msal4j"
}
```

|구분|버전|비고|
|---|---|---|
|vertx-core|3.7.1|Vertx Web server 구현|
|verx-web|3.7.1|Vertx Web server 구현|
|vertx-web-templ-jade|3.7.1|Jade Template Engine을 사용하여 Jade 파일을 HTML 파일로 렌더링|
|vertx-auth-oauth2|3.7.1|Vertx Web server에서 OAuth 2.0 인증 구현|
|msal4j|1.13.3|Vertx Web server에서 OAuth 2.0 인증 구현|

<p>
  주요 패키지 및 리소스 경로와 설명입니다.
</p>

|경로|패키지(리소스) 경로|파일|비고|
|---|-----------|-----|-------------|
|src/main/java|com.example.vertx.verticle.web|WebServerVerticle|웹 서버를 실행하는 버티클|
|src/main/java|com.example.vertx.verticle.web.auth|AdminAzureADLoginCallbackHandler|웹 서버에서 Azure AD 기반의 SSO 로그인 콜백 결과를 처리하는 핸들러|
|src/main/java|com.example.vertx.verticle.web.auth|AdminAzureADLoginHandler|웹 서버에서 Azure AD 기반의 SSO 로그인 요청을 처리하는 핸들러|
|src/main/java|com.example.vertx.verticle.web.auth|AdminAzureADUser|웹 서버에서 인증된 Azure AD 사용자 인스턴스|
|src/main/java|com.example.vertx.verticle.web.auth|AdminFormLoginAuthProvider|웹 서버에서 ID/Password Form 기반의 로그인을 검증하는 핸들러|
|src/main/java|com.example.vertx.verticle.web.auth|AdminFormLoginHandler|웹 서버에서 ID/Password Form 기반의 로그인을 처리하는 핸들러|
|src/main/java|com.example.vertx.verticle.web.auth|AdminFormUser|웹 서버에서 인증된 ID/Password Form 로그인 사용자 인스턴스|
|src/main/resources|/webroot/jade/template/admin|login.jade|로그인 페이지|
|src/main/resources|/webroot/jade/template/admin|main.jade|로그인에 성공하면 리다이렉션 되는 페이지|
|src/main/resources|/|config.xml|애플리케이션 환경 설정|

<h2>
  Run sample
</h2>
<p>
  src/main/resources/config.xml의 내용을 다음과 같이 입력합니다.
<p>
</p>

```xml
<?xml version="1.0"?>
<web>
    <port>8080</port>
    <logActivity>true</logActivity>
    <adminUserName>admin</adminUserName>
    <adminPassword>admin</adminPassword>
    <sessionTimeout>60000</sessionTimeout>
    <auzureADClientID>[클라이언트 ID]</auzureADClientID>
    <auzureADTenantID>[테넌트 ID]</auzureADTenantID>
    <auzureADClientKey>[클라이언트 키]</auzureADClientKey>
    <auzureADClientSecret>[클라이언트 시크릿]</auzureADClientSecret>
    <auzureADLoginRedirectURL>http://localhost:8080/api/login/azuread/callback</auzureADLoginRedirectURL>
</web>
```

<p>
  다음과 같이 Run configuration을 입력 후 소스 코드를 실행합니다.
</p>

|구분|값|
|---|---|
|VM options|-Dvertx.disableFileCaching=true -Dvertx.web.disableTemplCaching=true|
|Main class|io.vertx.core.Launcher|
|Program arugments|run com.example.vertx.verticle.launcher.Launcher|

<p>
  실행을 확인하려면 웹 브라우저에서 <a href="http://localhost:8080/admin/login">http://localhost:8080/admin/login</a>에 연결합니다.
</p>

<img width="1425" alt="webpage" src="https://user-images.githubusercontent.com/121745354/210195719-fdb46964-0788-4c90-9835-1ae0573ca9cb.png">

