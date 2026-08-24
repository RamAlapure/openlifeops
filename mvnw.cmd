@ECHO OFF
setlocal
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  set "MAVEN_HOME=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.5\plugins\maven\lib\maven3"
)
"%MAVEN_HOME%\bin\mvn.cmd" %*
