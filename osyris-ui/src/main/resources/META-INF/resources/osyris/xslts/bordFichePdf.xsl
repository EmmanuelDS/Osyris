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
        
        
       <xsl:for-each select="/verslag/bord">
       
       <fo:table border="solid" table-layout="fixed" width="100%" height="100%">
        <fo:table-header border="solid">
        	<fo:table-row>
                    <fo:table-cell border="solid">
                        <fo:block text-align="center" font-weight="bold">TOERISME OOST-VLAANDEREN</fo:block>
                        <fo:block text-align="center" font-weight="bold">Fiche 
	                        <xsl:value-of
			          			select="/verslag/trajecttype">
			          		</xsl:value-of></fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold">REGIO: 
                       	<xsl:value-of
	          				select="/verslag/regio">
		          		</xsl:value-of>
                        </fo:block>
                    </fo:table-cell>
           </fo:table-row>
        </fo:table-header>
        
        <fo:table-body>    
          <fo:table-row height="80mm" >
            <fo:table-cell border="solid" >
             
              <fo:block font-size="10pt">
				<fo:block space-after="10pt">id:
	          	<xsl:value-of
	          		select="./id">
	          	</xsl:value-of>
	          </fo:block>
	           <fo:block space-after="10pt">Gemeente:
	          	<xsl:value-of
	          		select="./gemeente">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="10pt">Straat:
	          	<xsl:value-of
	          		select="./straat">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="10pt">XY coördinaten in Lambert 72:
	          </fo:block>
	          <fo:block  space-after="10pt" text-indent="20mm">X:
	          	<xsl:value-of
	          		select="./x">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block text-indent="20mm" space-after="10pt">Y:
	          	<xsl:value-of
	          		select="./y">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="10pt">Wegbevoegd:
	          	<xsl:value-of
	          		select="./wegbevoegd">
	          	</xsl:value-of>
	          </fo:block>	
	          
				<xsl:if test="//@netwerkbord='true'">
					  <fo:block space-after="10pt">KnooppuntNr 0:
			          	<xsl:value-of
			          		select="./knooppunt0">
			          	</xsl:value-of>
			          </fo:block>
			          <fo:block space-after="10pt">KnooppuntNr 1:
			          	<xsl:value-of
			          		select="./knooppunt1">
			          	</xsl:value-of>
			          </fo:block>
			           <fo:block space-after="10pt">KnooppuntNr 2:
			          	<xsl:value-of
			          		select="./knooppunt2">
			          	</xsl:value-of>
			          </fo:block>
			           <fo:block space-after="10pt">KnooppuntNr 3:
			          	<xsl:value-of
			          		select="./knooppunt3">
			          	</xsl:value-of>
			          </fo:block>
				</xsl:if>	
						
              </fo:block>
            </fo:table-cell>
            
            <fo:table-cell>
           <fo:block font-size="10pt">
                 <fo:block space-after="10pt">Constructietype:
	          	<xsl:value-of
	          		select="./constructieType">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="10pt">Paaltype:
	          	<xsl:value-of
	          		select="./paaltype">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="10pt">paaldiameter:
	          	<xsl:value-of
	          		select="./paaldiameter">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="10pt">Paalbeugel:
	          	<xsl:value-of
	          		select="./paalbeugel">
	          	</xsl:value-of>
	          </fo:block>
			  <fo:block space-after="10pt">Paalondergrond:
	          	<xsl:value-of
	          		select="./paalondergrond">
	          	</xsl:value-of>
	          </fo:block>	
	          </fo:block>				
            </fo:table-cell>
           </fo:table-row>
           
           
           <fo:table-row height="90mm">
            <fo:table-cell border="solid">
            	 <fo:block>
	          	
	             </fo:block>	
	          </fo:table-cell>
	          	
             <fo:table-cell border="solid">
              <fo:block>
              
	          </fo:block>		
             </fo:table-cell>       
           </fo:table-row>
                
        </fo:table-body>
      </fo:table>
       </xsl:for-each>
       
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template name="bordProperties_left">
		<fo:block>
			<xsl:for-each select="/verslag/bord">
		
			</xsl:for-each>
		</fo:block>
  </xsl:template>
  
  <xsl:template name="bordProperties_right">
		<fo:block>
			<xsl:for-each select="/verslag/bord">
		
			</xsl:for-each>
		</fo:block>
  </xsl:template>
</xsl:stylesheet>