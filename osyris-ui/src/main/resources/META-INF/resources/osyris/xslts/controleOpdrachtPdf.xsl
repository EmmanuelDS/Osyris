<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
   
  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4-portrait"
              page-height="21cm" page-width="29.7cm" margin="1cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="A4-portrait">
        <fo:flow flow-name="xsl-region-body">
        
        <xsl:if test="//@netwerkCO='false'">
        <fo:block margin-left="40px" padding-bottom="20px">ROUTENAAM:
          	<xsl:value-of
			     select="/verslag/trajectnaam">
			</xsl:value-of>
        </fo:block>
        </xsl:if>
        
        <xsl:if test="//@netwerkCO='true'">
        <fo:block margin-left="40px" padding-bottom="20px">NAAM:
          	<xsl:value-of
			     select="/verslag/trajectnaam">
			</xsl:value-of>
        </fo:block>
        </xsl:if>

       <fo:table border="solid" table-layout="fixed" width="100%" height="100%">
        <fo:table-header>
        	<fo:table-row border="solid">
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold" font-size="10pt">BordNr</fo:block>
                    </fo:table-cell>
                    
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold" font-size="10pt">BordId</fo:block>
                    </fo:table-cell>
                    
                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Pijl</fo:block>
                    </fo:table-cell>
                    
                    <xsl:if test="//@netwerkCO='true'">
                       <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Bordtype</fo:block>
                    </fo:table-cell>
                    </xsl:if>
                    
                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Straatnaam</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Gemeente</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Type paal</fo:block>
                    </fo:table-cell>
           </fo:table-row>
        </fo:table-header>
        
        
        <fo:table-body border="solid">    
        
        <xsl:for-each select="/verslag/bord">
		<fo:table-row border="solid"  >
            <fo:table-cell>
              <fo:block font-size="10pt" text-align="center">
	          	<xsl:value-of
	          		select="./bordnr">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	          <fo:table-cell >
              <fo:block font-size="10pt" text-align="center">
	          	<xsl:value-of
	          		select="./id">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	          
	          <xsl:if test="//@netwerkCO='false'">
		          <xsl:if test="pijl='1'">
		          <fo:table-cell>
	              <fo:block font-size="10pt">
		          	<fo:block>
	        			<fo:external-graphic src="http://tov-osyris.gim.be/images/ar_green_e.png"/>
	     			</fo:block>
		          </fo:block>
		          </fo:table-cell>
		          </xsl:if>
		          <xsl:if test="pijl='2'">
		          <fo:table-cell>
	              <fo:block font-size="10pt">
		          	<fo:block>
	        			<fo:external-graphic src="http://tov-osyris.gim.be/images/ar_green_w.png"/>
	     			</fo:block>
		          </fo:block>
		          </fo:table-cell>
		          </xsl:if>
		           <xsl:if test="pijl='3'">
		          <fo:table-cell>
	              <fo:block font-size="10pt">
		          	<fo:block>
	        			<fo:external-graphic src="http://tov-osyris.gim.be/images/ar_green_n.png"/>
	     			</fo:block>
		          </fo:block>
		          </fo:table-cell>
		          </xsl:if>
	          </xsl:if>
	          
	          <xsl:if test="//@netwerkCO='true'">
	           <fo:table-cell>
	           <fo:block>
		          <xsl:if test="pijlkp1='1' or pijlkp2='1' or pijlkp3='1'">
	        			<fo:external-graphic src="http://tov-osyris.gim.be/images/ar_green_e.png"/>
		          </xsl:if>
		          <xsl:if test="pijlkp1='2' or pijlkp2='2' or pijlkp3='2'">
	        			<fo:external-graphic src="http://tov-osyris.gim.be/images/ar_green_w.png"/>
		          </xsl:if>
		           <xsl:if test="pijlkp1='3' or pijlkp2='3' or pijlkp3='3'">
	        			<fo:external-graphic src="http://tov-osyris.gim.be/images/ar_green_n.png"/>
		          </xsl:if>
		          </fo:block>
		         </fo:table-cell>
	          </xsl:if>
	          
	          <xsl:if test="//@netwerkCO='true'">
	          <fo:table-cell>
              <fo:block font-size="10pt">
	          	<xsl:value-of
	          		select="./bordtype">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	          </xsl:if>
	          
	          <fo:table-cell>
              <fo:block font-size="10pt">
	          	<xsl:value-of
	          		select="./straatnaam">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	           <fo:table-cell>
              <fo:block font-size="10pt">
	          	<xsl:value-of
	          		select="./gemeente">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	          <fo:table-cell>
              <fo:block font-size="10pt">
	          	<xsl:value-of
	          		select="./paaltype">
	          	</xsl:value-of>
	          </fo:block>
            </fo:table-cell>
           </fo:table-row> 
          </xsl:for-each>   
        </fo:table-body>
      </fo:table>

        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>