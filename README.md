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
  <li>키 값</li>
  <li>비밀 ID</li>
  <li>웹 리디렉션 URL</li>
</ul>
<h2>
  Sample code overview
</h2>
<p>
  build.gradle에서 다음 의존성을 포함합니다.
</p>

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
|---|----------------|---|---|
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

