<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
   
  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
      <fo:simple-page-master master-name="A3-landscape"
					page-height="297mm"
					page-width="420mm">
					<fo:region-body margin="10mm" />
				</fo:simple-page-master>
				
        <fo:simple-page-master master-name="A4-landscape"
              page-height="21cm" page-width="29.7cm" margin="1cm">
          <fo:region-body/>
           <fo:region-after region-name="footer" />
        </fo:simple-page-master>
      </fo:layout-master-set>
      
      <!-- OVERVIEW MAP A3 -->
      <fo:page-sequence master-reference="A3-landscape">     
       <fo:static-content flow-name="footer">
    	<fo:block text-align="center" font-size="10pt" color="#000000">
      	<fo:inline><fo:page-number/></fo:inline>
    	</fo:block>
   	   </fo:static-content> 
        <fo:flow flow-name="xsl-region-body">
        <xsl:call-template name="overviewMap" />
        </fo:flow>
      </fo:page-sequence>
        
      
      <!-- BEWEGWIJZERINGSTABEL A4 -->
      <fo:page-sequence master-reference="A4-landscape">    
       <fo:static-content flow-name="footer">
    	<fo:block text-align="center" font-size="10pt" color="#000000">
      	<fo:inline><fo:page-number/></fo:inline>
    	</fo:block>
   	   </fo:static-content>
   	  
        <fo:flow flow-name="xsl-region-body">
        
<!--         <xsl:call-template name="overviewMap" /> -->
         
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
       
       <!-- COLUMN WIDTH ROUTE CO -->
       <xsl:if test="//@netwerkCO='false'">
	       <fo:table-column column-width="10%"/>
	       <fo:table-column column-width="10%"/>
	       <fo:table-column column-width="10%"/>
	       <fo:table-column column-width="25%"/>
	       <fo:table-column column-width="20%"/>
	       <fo:table-column column-width="20%"/>
       </xsl:if>
       
       <!-- COLUMN WIDTH NETWERK CO -->
        <xsl:if test="//@netwerkCO='true'">
	       <fo:table-column column-width="10%"/>
	       <fo:table-column column-width="5%"/>
	       <fo:table-column column-width="5%"/>
	       <fo:table-column column-width="5%"/>
	       <fo:table-column column-width="5%"/>
	       <fo:table-column column-width="10%"/>
	       <fo:table-column column-width="25%"/>
	       <fo:table-column column-width="15%"/>
	       <fo:table-column column-width="15%"/>
       </xsl:if>

        <fo:table-header>
        	<fo:table-row border="solid">
<!--                     <fo:table-cell> -->
<!--                         <fo:block text-align="center" font-weight="bold" font-size="10pt">BordNr</fo:block> -->
<!--                     </fo:table-cell> -->
                    
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold" font-size="10pt">BordId</fo:block>
                    </fo:table-cell>
                    
                    <xsl:if test="//@netwerkCO='false'">                     
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold" font-size="10pt">BordNr</fo:block>
                    </fo:table-cell>

                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Pijl</fo:block>
                    </fo:table-cell>
                     </xsl:if>
                    
                    <xsl:if test="//@netwerkCO='true'">                   
                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Kp0</fo:block>                        
                    </fo:table-cell>
                    
                     <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Kp1</fo:block>                        
                    </fo:table-cell>
                    
                     <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Kp2</fo:block>                        
                    </fo:table-cell>
                    
                     <fo:table-cell>
                        <fo:block font-weight="bold" font-size="10pt">Kp3</fo:block>                        
                    </fo:table-cell>
                    
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
<!--             <fo:table-cell> -->
<!--               <fo:block font-size="10pt" text-align="center"> -->
<!-- 	          	<xsl:value-of -->
<!-- 	          		select="./bordnr"> -->
<!-- 	          	</xsl:value-of> -->
<!-- 	          </fo:block> -->
<!-- 	          </fo:table-cell> -->
	          <fo:table-cell >
              <fo:block font-size="10pt" text-align="center">
	          	<xsl:value-of
	          		select="./id">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	          
	          <xsl:if test="//@netwerkCO='false'">
	          <fo:table-cell>
              <fo:block font-size="10pt" text-align="center">
	          	<xsl:value-of
	          		select="./bordnr">
	          	</xsl:value-of>
	          </fo:block>
	          </fo:table-cell>
	          <fo:table-cell>
		           <fo:block font-size="10pt">
		            <xsl:variable name="url_pijl" select="./pijl" />	
			            <fo:external-graphic src="'{$url_pijl}'"/>
		           </fo:block>
	          </fo:table-cell>
	          </xsl:if>
	     
	          <xsl:if test="//@netwerkCO='true'">
	          
	          <fo:table-cell>
	              <fo:block font-size="10pt">
		          	<xsl:value-of
		          		select="./kp0">
		          	</xsl:value-of>
		          </fo:block>
	          </fo:table-cell>
	          
	          <fo:table-cell>
	            <fo:block font-size="10pt">
		            <xsl:variable name="url_kp1_pijl" select="./pijlkp1" />	
			            <fo:external-graphic src="'{$url_kp1_pijl}'"/>				            
			            <xsl:value-of select="./kp1"></xsl:value-of>
			   </fo:block>
	          </fo:table-cell>
			        
			  <fo:table-cell>
	            <fo:block font-size="10pt">      
		            <xsl:variable name="url_kp2_pijl" select="./pijlkp2" />	
			            <fo:external-graphic src="'{$url_kp2_pijl}'"/>
			            <xsl:value-of select="./kp2"></xsl:value-of>
			   </fo:block>
	          </fo:table-cell>
	          
	          <fo:table-cell>  
	          	<fo:block font-size="10pt">         
		            <xsl:variable name="url_kp3_pijl" select="./pijlkp3" />	
			            <fo:external-graphic src="'{$url_kp3_pijl}'"/>
			            <xsl:value-of select="./kp3"></xsl:value-of>
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
  
  <xsl:template name="overviewMap">
  <fo:table table-layout="auto">
	<fo:table-body>
	<fo:table-row>
      <fo:table-cell >
      
	  	<fo:block text-align="center">		
			<xsl:variable name="overviewMap_url" select="/verslag/overviewMap" />
			<fo:external-graphic src="'{$overviewMap_url}'"
			content-width="auto" content-height="auto" width="auto" height="auto"/>
<!-- 				content-height="280mm" -->
<!-- 				content-width="420mm"/> -->
	     </fo:block>
	     		
     </fo:table-cell>
     </fo:table-row>
	 </fo:table-body>
	 </fo:table>
<!-- 	 <fo:block page-break-after="always"/>  -->
	 
  </xsl:template>
</xsl:stylesheet>