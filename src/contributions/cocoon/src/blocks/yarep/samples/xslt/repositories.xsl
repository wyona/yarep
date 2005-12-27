<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  >

<xsl:template match="repositories">
<html>
<body>
<h1>Repositories</h1>
<table border="1">
<tr>
  <th>ID</th>
  <th>Name</th>
  <th>Configuration File</th>
</tr>
<xsl:for-each select="repository">
<tr>
  <td><a href="repo-{@id}/"><xsl:value-of select="@id"/></a></td>
  <td><xsl:value-of select="."/></td>
  <td><xsl:value-of select="@file"/></td>
</tr>
</xsl:for-each>
</table>
</body>
</html>
</xsl:template>

</xsl:stylesheet>
