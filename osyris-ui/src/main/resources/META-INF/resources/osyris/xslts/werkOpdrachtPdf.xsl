<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
   
  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4-landscape"
              page-height="21cm" page-width="29.7cm" margin="1cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="A4-landscape">
        <fo:flow flow-name="xsl-region-body">

<fo:table>
<fo:table-body>
	<fo:table-row >
      <fo:table-cell >
        <fo:block margin-left="30px" padding-bottom="20px">
        <fo:inline font-weight="bold"><xsl:text>Opdrachtnummer: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/id">
			</xsl:value-of>
        </fo:block>
        
         <xsl:if test="//@segment='false'">
        <fo:block margin-left="30px" padding-bottom="20px">
        <fo:inline font-weight="bold"><xsl:text>Traject: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/trajectnaam">
			</xsl:value-of>
        </fo:block>
        </xsl:if>
        
        <xsl:if test="//@segment='true'">
        <fo:block margin-left="30px" padding-bottom="20px">
        <fo:inline font-weight="bold"><xsl:text>Van knooppunt: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/vankp">
			</xsl:value-of>
        </fo:block>
        <fo:block margin-left="30px" padding-bottom="20px">
        <fo:inline font-weight="bold"><xsl:text>Naar knooppunt: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/naarkp">
			</xsl:value-of>
        </fo:block>
        </xsl:if>
        
        <xsl:if test="//@bordprobleem='true'">
         <fo:block margin-left="30px" padding-bottom="20px">
         <fo:inline font-weight="bold"><xsl:text>Gemeente: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/gemeente">
			</xsl:value-of>
        </fo:block>
            <fo:block margin-left="30px" padding-bottom="20px">
            <fo:inline font-weight="bold"><xsl:text>Straat: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/straat">
			</xsl:value-of>
        </fo:block>
        
         <fo:block margin-left="30px">
         <fo:inline font-weight="bold"><xsl:text>Paaltype: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/paaltype">
          	</xsl:value-of>
	     </fo:block>
	     <fo:block margin-left="30px">
	     <fo:inline font-weight="bold"><xsl:text>Paaldiameter: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/paaldiameter">
          	</xsl:value-of>
          </fo:block>
          <fo:block margin-left="30px">
          <fo:inline font-weight="bold"><xsl:text>Paalbeugel: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/paalbeugel">
          	</xsl:value-of>
          </fo:block>
		  <fo:block margin-left="30px" margin-bottom="20px">
		  <fo:inline font-weight="bold"><xsl:text>Paalondergrond: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/paalondergrond">
          	</xsl:value-of>
          </fo:block>	
        </xsl:if> 
        
        <fo:block margin-left="30px" margin-bottom="20px">
         	<fo:inline font-weight="bold"><xsl:text>Opdracht: </xsl:text></fo:inline>
        </fo:block>
         
         <xsl:for-each select="/opdracht/handeling">
          <fo:block margin-left="30px" margin-bottom="20px">
          	<xsl:value-of
          		select="./nummer">
          	</xsl:value-of>
          	<fo:inline><xsl:text>. </xsl:text></fo:inline>
          	<xsl:value-of
          		select="./type">
          	</xsl:value-of>
	     </fo:block>
	     
         </xsl:for-each>   
		</fo:table-cell>
		
        <fo:table-cell >
              <fo:block>
              
               <xsl:if test="//@hasFoto='true'">
               <xsl:variable name="foto" select="/opdracht/foto" />
               <fo:external-graphic src="data:image/jpeg;base64,{$foto}"/>
               </xsl:if>
			   <xsl:if test="//@hasFoto='false'">
          		<fo:external-graphic src="http://osyristest.tov.be/fotos/geen%20foto.png"  />
                </xsl:if>
            		
        </fo:block>
        </fo:table-cell>
        </fo:table-row>
        </fo:table-body>
 </fo:table>           
        
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>