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
          <fo:region-after region-name="footer" />
        </fo:simple-page-master>
      </fo:layout-master-set>
   	  
   	  <!-- NEW PAGE SEQUENCE FOR EVERY PAGE TO AVOID HEAP SPACE MEMORY ISSUES -->
      <xsl:for-each select="/verslag/bord">
      <fo:page-sequence master-reference="A4-landscape">
       
       <fo:static-content flow-name="footer">
    	<fo:block text-align="center" font-size="10pt" color="#000000">
      	<fo:inline><fo:page-number/></fo:inline>
    	</fo:block>
   	  </fo:static-content>
   	  
        <fo:flow flow-name="xsl-region-body">
        
       <fo:table border="solid" table-layout="fixed" width="100%" height="100%">
        <fo:table-header border="solid">
        	<fo:table-row>
                    <fo:table-cell border="solid">
                        <fo:block text-align="center" font-weight="bold">TOERISME OOST-VLAANDEREN</fo:block>
                        <fo:block text-align="center" font-weight="bold">Fiche 
	                        <fo:inline color="red">
		                        <xsl:value-of
				          			select="/verslag/trajecttype">	
				          		</xsl:value-of>
				          		
			          		     <xsl:text> </xsl:text>
				          		 <xsl:value-of
				          			select="/verslag/trajectnaam">
				          		</xsl:value-of>
				          	</fo:inline> 
			          	</fo:block>
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
            
             <fo:block-container>	
             	<xsl:if test="//@netwerkbord='false'">											
	             	<fo:block margin-left="0.1cm" margin-top="0.1cm">
	            		<xsl:variable name="url_voorbeeldFoto" select="/verslag/voorbeeldFoto" />	
			            <fo:external-graphic src="'{$url_voorbeeldFoto}'"
			            content-width="60mm"
	            		content-height="90mm" />
	       			 </fo:block>
       			 </xsl:if>
       			 
       			 <xsl:if test="//@netwerkbord='true'">											
	             	<fo:block margin-left="0.1cm" margin-top="0.1cm">
	            		<xsl:variable name="url_voorbeeld_kp_bord" select="./voorbeeld_kp_bord" />	
			            <fo:external-graphic src="'{$url_voorbeeld_kp_bord}'"
			           	  content-width="55mm"
	            		  content-height="79mm" />
	       			 </fo:block>
       			 </xsl:if>
             </fo:block-container>
             
             
             <fo:block-container position="absolute">
              <fo:block font-size="9pt" margin-top="0.3cm" margin-left="7cm">
				<fo:block space-after="8pt">
				<fo:inline font-weight="bold"><xsl:text>Id: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./id">
	          	</xsl:value-of>
	          </fo:block>
	           <fo:block space-after="8pt">
	           <fo:inline font-weight="bold"><xsl:text>Gemeente: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./gemeente">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Straat: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./straat">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>XY coördinaten in Lambert 72: </xsl:text></fo:inline>
	          </fo:block>
	          <fo:block  space-after="8pt" text-indent="20mm">
	          <fo:inline font-weight="bold"><xsl:text>X: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./x">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block text-indent="20mm" space-after="10pt">
	          <fo:inline font-weight="bold"><xsl:text>Y: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./y">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Wegbevoegd: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./wegbevoegd">
	          	</xsl:value-of>
	          </fo:block>	
	          
	          <xsl:if test="//@netwerkbord='false'">
	          
	           <fo:block space-after="8pt">
		          <fo:inline font-weight="bold"><xsl:text>Volgnr: </xsl:text></fo:inline>
		          	<xsl:value-of
		          		 select="./volg">
		          	</xsl:value-of>
		          </fo:block>
		          
		          <fo:block space-after="8pt">
		          <xsl:variable name="url_pijl" select="./pijl" />	
		          <fo:inline font-weight="bold" padding-end="5pt"><xsl:text>Pijl: </xsl:text></fo:inline>
		            <fo:external-graphic src="'{$url_pijl}'" padding-start="5pt"/>
		          </fo:block>
		          
		          <fo:block space-after="8pt">
		          <fo:inline font-weight="bold"><xsl:text>Opschrift bord: </xsl:text></fo:inline>
		          	<xsl:value-of
		          		 select="/verslag/trajectnaam">
		          	</xsl:value-of>
		          </fo:block>
		       </xsl:if>
	          
				<xsl:if test="//@netwerkbord='true'">
					  <fo:block space-after="8pt">
					  <fo:inline font-weight="bold" padding-end="5pt"><xsl:text>KnooppuntNr 0:</xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="./knooppunt0">	
			          	</xsl:value-of>		         			          	
			          </fo:block>
			          <fo:block space-after="8pt">
			          <fo:inline font-weight="bold" padding-end="5pt"><xsl:text>KnooppuntNr 1:</xsl:text></fo:inline>
			            <xsl:value-of
			          		select="./knooppunt1">
			          	</xsl:value-of>			          
			          	<xsl:variable name="url_kp1_pijl" select="./kp1_pijl" />	
					            <fo:external-graphic src="'{$url_kp1_pijl}'" padding-start="5pt"/>
			          </fo:block>
			           <fo:block space-after="8pt">
			           <fo:inline font-weight="bold" padding-end="5pt"><xsl:text>KnooppuntNr 2:</xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="./knooppunt2">
			          	</xsl:value-of>
			          	<xsl:variable name="url_kp2_pijl" select="./kp2_pijl" />	
					            <fo:external-graphic src="'{$url_kp2_pijl}'" padding-start="5pt"/>
			          </fo:block>
			           <fo:block space-after="8pt">
			           <fo:inline font-weight="bold" padding-end="5pt"><xsl:text>KnooppuntNr 3:</xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="./knooppunt3">
			          	</xsl:value-of>
			          	<xsl:variable name="url_kp3_pijl" select="./kp3_pijl" />	
					            <fo:external-graphic src="'{$url_kp3_pijl}'" padding-start="5pt"/>
			          </fo:block>
				</xsl:if>				
              </fo:block>
             </fo:block-container>
            </fo:table-cell>
            
            
            <fo:table-cell>
            <fo:block-container>
             <xsl:variable name="url_bordfoto" select="./bordfoto" />													
             	<fo:block margin-left="0.1cm">
            		<fo:external-graphic src="'{$url_bordfoto}'" 
            		content-width="60mm"
            		content-height="80mm" />
       			 </fo:block>
             </fo:block-container>
           
          <fo:block-container position="absolute">
           <fo:block font-size="9pt" margin-top="0.3cm" margin-bottom="0.1cm" margin-left="7cm">
               <fo:block space-after="8pt">
                 <fo:inline font-weight="bold"><xsl:text>Constructietype: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./constructieType">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Paaltype: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./paaltype">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Paaldiameter: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./paaldiameter">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Paalbeugel: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./paalbeugel">
	          	</xsl:value-of>
	          </fo:block>
			  <fo:block space-after="8pt">
			  <fo:inline font-weight="bold"><xsl:text>Paalondergrond: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="./paalondergrond">
	          	</xsl:value-of>
	          </fo:block>	
	          </fo:block>	
	          </fo:block-container>			
            </fo:table-cell>
           </fo:table-row>
              
           <fo:table-row height="90mm">
            <fo:table-cell border="solid">
            	 <fo:block>
					<xsl:variable name="mapTopo_url" select="./mapTopo" />
			<fo:external-graphic src="url(file:/{$mapTopo_url})"
							content-width="137mm"
		            		content-height="100mm" />
<!-- 					<fo:external-graphic src="'{$mapTopo_url}'" -->
<!-- 							content-width="137mm" -->
<!-- 		            		content-height="100mm" /> -->
	             </fo:block>	
	          </fo:table-cell>
	          	
             <fo:table-cell border="solid">
              <fo:block>
				<xsl:variable name="mapOrtho_url" select="./mapOrtho" />
				<fo:external-graphic src="url(file:/{$mapOrtho_url})"
						content-width="137mm"
	            		content-height="100mm" />
<!-- 				<fo:external-graphic src="'{$mapOrtho_url}'" -->
<!-- 						content-width="137mm" -->
<!-- 	            		content-height="100mm" /> -->
		      </fo:block>		
             </fo:table-cell>       
           </fo:table-row>
                
        </fo:table-body>
      	</fo:table>
        </fo:flow>
      </fo:page-sequence>
      </xsl:for-each>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>