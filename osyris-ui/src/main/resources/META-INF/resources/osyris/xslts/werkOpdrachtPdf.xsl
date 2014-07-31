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
      <fo:page-sequence master-reference="A4-landscape">
      
      
      <fo:static-content flow-name="footer">
    	<fo:block text-align="center" font-size="10pt" color="#000000">
      	<fo:inline><fo:page-number/></fo:inline>
    	</fo:block>
   	  </fo:static-content>
  
        <fo:flow flow-name="xsl-region-body">

		 <xsl:if test="//@bordProbleem='false'">
		 	<xsl:call-template name="anderProbleemFiche" />
		 </xsl:if>
		 
		 <xsl:if test="//@bordProbleem='true'">
			<xsl:call-template name="bordFiche" />
		 </xsl:if>         
        
        </fo:flow>
      </fo:page-sequence>    
    </fo:root>
  </xsl:template>
  
    <xsl:template name="anderProbleemFiche">
		<fo:table border="solid" table-layout="fixed" width="100%" height="100%">
        <fo:table-header border="solid">
        	<fo:table-row>
                    <fo:table-cell border="solid">
                        <fo:block text-align="center" font-weight="bold">TOERISME OOST-VLAANDEREN</fo:block>
              			 <fo:block text-align="center" font-weight="bold">
              			 <fo:inline font-weight="bold"><xsl:text>Opdrachtnummer: </xsl:text></fo:inline>
         					<xsl:value-of
		    					 select="/opdracht/id">
							</xsl:value-of>	                       
			          	</fo:block>			
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold">REGIO: 
                       		<xsl:value-of
	          				select="/opdracht/regio">
		          			</xsl:value-of>
		          		
		          		 	<fo:block text-align="center" font-weight="bold">
	                        <fo:inline color="red">
		                        <xsl:value-of
				          			select="/opdracht/trajecttype">	
				          		</xsl:value-of>
				          		
				          		<xsl:if test="//@segment='false'">
				          		     <xsl:text> </xsl:text>
					          		 <xsl:value-of
					          			select="/opdracht/trajectnaam">
					          		</xsl:value-of>
				          		</xsl:if>
				          		
				          	</fo:inline> 
			          	</fo:block>		          				          					          			      
                        </fo:block>
                    </fo:table-cell>
           </fo:table-row>
        </fo:table-header>
        
        <fo:table-body>    
          <fo:table-row height="80mm" >
            <fo:table-cell border="solid" >
            											
              <fo:block font-size="10pt" margin-left="1cm" margin-top="0.5cm">
				<fo:block space-after="8pt">
			        <fo:inline font-weight="bold"><xsl:text>Gemeente: </xsl:text></fo:inline>
			          	<xsl:value-of
						     select="/opdracht/gemeente">
						</xsl:value-of>
			    </fo:block>
			   
				<fo:block space-after="8pt">
			        <fo:inline font-weight="bold"><xsl:text>Straat dichtstbijzijnde bord: </xsl:text></fo:inline>
			          	<xsl:value-of
						     select="/opdracht/straat">
						</xsl:value-of>
			    </fo:block>
			        
				<xsl:if test="//@segment='true'">
			        <fo:block space-after="8pt">
			        <fo:inline font-weight="bold"><xsl:text>Segment knooppunt van: </xsl:text></fo:inline>
			          	<xsl:value-of
						     select="/opdracht/vankp">
						</xsl:value-of>
			        </fo:block>
			        <fo:block space-after="8pt">
			        <fo:inline font-weight="bold"><xsl:text>Segment knooppunt naar: </xsl:text></fo:inline>
			          	<xsl:value-of
						     select="/opdracht/naarkp">
						</xsl:value-of>
        			</fo:block>
       			</xsl:if>
						
				<fo:block space-after="8pt">
		       		<fo:inline font-weight="bold"><xsl:text>Werkhandelingen: </xsl:text></fo:inline>
		      	</fo:block>    
		       	<xsl:for-each select="/opdracht/handeling">
			        <fo:block margin-left="20px" margin-bottom="8px">
			        	<xsl:value-of
			        		select="./nummer">
			        	</xsl:value-of>
			        	<fo:inline><xsl:text>. </xsl:text></fo:inline>
			        	<xsl:value-of
			        		select="./type">
			        	</xsl:value-of>
			      	</fo:block>
		      	</xsl:for-each>
		      		<fo:block space-after="8pt">
					  <fo:inline font-weight="bold"><xsl:text>Commentaar TOV: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/commentaar">
			          	</xsl:value-of>
				 	</fo:block>	
       		 </fo:block>
 
            </fo:table-cell>
                    
            <fo:table-cell>											
             	<fo:block margin-left="0.1cm" text-align="center">
            		<xsl:if test="//@hasFoto='true'">
	               <xsl:variable name="foto" select="/opdracht/probleemfoto" />
	               <fo:external-graphic src="data:image/jpeg;base64,{$foto}" 
	               	content-width="137mm"
            		content-height="80mm" />
	              </xsl:if>
				   <xsl:if test="//@hasFoto='false'">
	          		<xsl:variable name="url_geen_foto" select="/opdracht/probleemfoto" />	
		            <fo:external-graphic src="'{$url_geen_foto}'"
		            content-width="137mm"
            		content-height="80mm" />
	               </xsl:if>
       			</fo:block>		
            </fo:table-cell>
           </fo:table-row>
              
           <fo:table-row height="80mm">
            <fo:table-cell border="solid">
            	 <fo:block margin-left="0.1cm">
					<xsl:variable name="mapTopoAnderProbleem_url" select="/opdracht/mapTopoAnderProbleem" />
<!-- 				<fo:external-graphic src="'{$mapTopoAnderProbleem_url}'" -->
<!-- 						content-width="137mm" -->
<!-- 		            	content-height="100mm" /> -->
					<fo:external-graphic src="url(file:/{$mapTopoAnderProbleem_url})"
							content-width="137mm"
		            		content-height="100mm" />
	             </fo:block>	
	          </fo:table-cell>
	          	
             <fo:table-cell border="solid">
              <fo:block margin-left="0.1cm">
				<xsl:variable name="mapOrthoAnderProbleem_url" select="/opdracht/mapOrthoAnderProbleem" />
<!-- 				<fo:external-graphic src="'{$mapOrthoAnderProbleem_url}'" -->
<!-- 						content-width="137mm" -->
<!-- 	            		content-height="100mm" /> -->
					<fo:external-graphic src="url(file:/{$mapOrthoAnderProbleem_url})"
							content-width="137mm"
		            		content-height="100mm" />
		      </fo:block>		
             </fo:table-cell>       
           </fo:table-row>            
        </fo:table-body>
      </fo:table> 
  </xsl:template>
  
  
  
  <xsl:template name="bordFiche">
  		<fo:block page-break-before="always"/> 
		<fo:block>
		<fo:table border="solid" table-layout="fixed" width="100%" height="100%">
        <fo:table-header border="solid">
        	<fo:table-row>
                    <fo:table-cell border="solid">
                        <fo:block text-align="center" font-weight="bold">TOERISME OOST-VLAANDEREN</fo:block>
                        <fo:block text-align="center" font-weight="bold">
		          			<fo:inline font-weight="bold"><xsl:text>Opdrachtnummer: </xsl:text></fo:inline>
         					<xsl:value-of
		    					 select="/opdracht/id">
							</xsl:value-of>
		          		</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold">REGIO: 
                       	<xsl:value-of
	          				select="/opdracht/regio">
		          		</xsl:value-of>
		          		
		          		 <fo:block text-align="center" font-weight="bold">
	                        <fo:inline color="red">
		                        <xsl:value-of
				          			select="/opdracht/trajecttype">	
				          		</xsl:value-of>
				          		
				          		<xsl:if test="//@netwerkbord='false'">
				          		     <xsl:text> </xsl:text>
					          		 <xsl:value-of
					          			select="/opdracht/trajectnaam">
					          		</xsl:value-of>
				          		</xsl:if>
				          		
				          	</fo:inline> 
			          	</fo:block>			          			      
                        </fo:block>
                    </fo:table-cell>
           </fo:table-row>
        </fo:table-header>
        
        <fo:table-body>    
          <fo:table-row height="80mm" >
            <fo:table-cell border="solid" >
            
             <fo:block-container>												
             	<fo:block margin-left="0.1cm">
             	<xsl:if test="//@netwerkbord='false'">		
            		<xsl:variable name="url_voorbeeldFoto" select="/opdracht/voorbeeldFoto" />	
		            <fo:external-graphic src="'{$url_voorbeeldFoto}'"
		            content-width="55mm"
            		content-height="79mm" />
            	</xsl:if>
            	
            	<xsl:if test="//@netwerkbord='true'">											
	             	<fo:block margin-left="0.1cm" margin-top="0.1cm">
	            		<xsl:variable name="url_voorbeeld_kp_bord" select="/opdracht/voorbeeld_kp_bord" />	
			            <fo:external-graphic src="'{$url_voorbeeld_kp_bord}'"
			           	  content-width="55mm"
	            		  content-height="79mm" />
	       			 </fo:block>
       			 </xsl:if>
       			 </fo:block>
             </fo:block-container>
             
             <fo:block-container position="absolute">
              <fo:block font-size="8pt" margin-top="0.3cm" margin-left="7cm">
				<fo:block space-after="8pt">
				<fo:inline font-weight="bold"><xsl:text>Id: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/bordId">
	          	</xsl:value-of>
	          </fo:block>
	           <fo:block space-after="8pt">
	           <fo:inline font-weight="bold"><xsl:text>Gemeente: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/bordGemeente">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Straat: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/bordStraat">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>XY coördinaten in Lambert 72: </xsl:text></fo:inline>
	          </fo:block>
	          <fo:block  space-after="8pt" text-indent="20mm">
	          <fo:inline font-weight="bold"><xsl:text>X: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/x">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block text-indent="20mm" space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Y: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/y">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Wegbevoegd: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/wegbevoegd">
	          	</xsl:value-of>
	          </fo:block>	
	          
	          <xsl:if test="//@netwerkbord='false'">
		          <fo:block space-after="8pt">
		          <xsl:variable name="url_pijl" select="/opdracht/bord/pijl" />	
		          <fo:inline font-weight="bold"><xsl:text>Pijl: </xsl:text></fo:inline>
		            <fo:external-graphic src="'{$url_pijl}'"/>
		          </fo:block>

		          <fo:block space-after="8pt">
		          <fo:inline font-weight="bold"><xsl:text>Opschrift bord: </xsl:text></fo:inline>
		          	<xsl:value-of
		          		 select="/opdracht/trajectnaam">
		          	</xsl:value-of>
		          </fo:block>
	           </xsl:if>
	          
				<xsl:if test="//@netwerkbord='true'">
					  <fo:block space-after="8pt">
					  <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 0: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt0">
			          	</xsl:value-of>
			          </fo:block>
			          <fo:block space-after="8pt">
			          <xsl:variable name="url_kp1_pijl" select="/opdracht/bord/kp1_pijl" />
			          <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 1: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt1">
			          	</xsl:value-of>
			          	<xsl:text>   </xsl:text>
			          	<fo:external-graphic src="'{$url_kp1_pijl}'"/>
			          </fo:block>
			           <fo:block space-after="8pt">
			           <xsl:variable name="url_kp2_pijl" select="/opdracht/bord/kp2_pijl" />
			           <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 2: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt2">
			          	</xsl:value-of>
			          	<fo:external-graphic src="'{$url_kp2_pijl}'"/>
			          </fo:block>
			           <fo:block space-after="8pt">
			           <xsl:variable name="url_kp3_pijl" select="/opdracht/bord/kp3_pijl" />
			           <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 3: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt3">
			          	</xsl:value-of>
			          	<fo:external-graphic src="'{$url_kp3_pijl}'"/>
			          </fo:block>
				</xsl:if>				
              </fo:block>
             </fo:block-container>
            </fo:table-cell>
                    
            <fo:table-cell>
             <fo:block-container>												
             	<fo:block margin-left="0.1cm">
	               <xsl:variable name="url_bordfoto" select="/opdracht/bordfoto" />
	                <fo:external-graphic src="'{$url_bordfoto}'" 
	               	content-width="60mm"
            		content-height="90mm" />
       			 </fo:block>
             </fo:block-container>
             
<!--             <fo:block-container>												 -->
<!--              	<fo:block margin-left="0.1cm"> -->
<!--             		<xsl:if test="//@hasFoto='true'"> -->
<!-- 	               <xsl:variable name="foto" select="/opdracht/probleemfoto" /> -->
<!-- 	               <fo:external-graphic src="data:image/jpeg;base64,{$foto}"  -->
<!-- 	               	content-width="60mm" -->
<!--             		content-height="90mm" /> -->
<!-- 	              </xsl:if> -->
<!-- 				   <xsl:if test="//@hasFoto='false'"> -->
<!-- 	          		<xsl:variable name="url_geen_foto" select="/opdracht/probleemfoto" />	 -->
<!-- 		            <fo:external-graphic src="'{$url_geen_foto}'" -->
<!-- 		            content-width="60mm" -->
<!--             		content-height="90mm" /> -->
<!-- 	               </xsl:if> -->
<!--        			 </fo:block> -->
<!--              </fo:block-container> -->
           
          <fo:block-container position="absolute">
           <fo:block font-size="8pt" margin-top="0.3cm" margin-bottom="0.1cm" margin-left="7cm">
               <fo:block space-after="10pt">
                 <fo:inline font-weight="bold"><xsl:text>Constructietype: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/constructieType">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Paaltype: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/paaltype">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Paaldiameter: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/paaldiameter">
	          	</xsl:value-of>
	          </fo:block>
	          <fo:block space-after="8pt">
	          <fo:inline font-weight="bold"><xsl:text>Paalbeugel: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/paalbeugel">
	          	</xsl:value-of>
	          </fo:block>
			  <fo:block space-after="8pt">
			  <fo:inline font-weight="bold"><xsl:text>Paalondergrond: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/bord/paalondergrond">
	          	</xsl:value-of>
	          </fo:block>	
	          
		      <fo:block space-after="8pt">
		       	<fo:inline font-weight="bold"><xsl:text>Opdracht: </xsl:text></fo:inline>
		      </fo:block>    
		       <xsl:for-each select="/opdracht/handeling">
		        <fo:block margin-left="20px" margin-bottom="8px">
		        	<xsl:value-of
		        		select="./nummer">
		        	</xsl:value-of>
		        	<fo:inline><xsl:text>. </xsl:text></fo:inline>
		        	<xsl:value-of
		        		select="./type">
		        	</xsl:value-of>
		      </fo:block>
		      </xsl:for-each>
	     
	     	  <fo:block space-after="8pt">
			  <fo:inline font-weight="bold"><xsl:text>Commentaar TOV: </xsl:text></fo:inline>
	          	<xsl:value-of
	          		select="/opdracht/commentaar">
	          	</xsl:value-of>
	          </fo:block>	
	          
  	          </fo:block>	
	          </fo:block-container>			
            </fo:table-cell>
           </fo:table-row>
              
           <fo:table-row height="80mm">
            <fo:table-cell border="solid">
            	 <fo:block margin-left="0.1cm">
					<xsl:variable name="mapTopo_url" select="/opdracht/bord/mapTopo" />
<!-- 					<fo:external-graphic src="'{$mapTopo_url}'" -->
<!-- 							content-width="137mm" -->
<!-- 		            		content-height="100mm" /> -->
					<fo:external-graphic src="url(file:/{$mapTopo_url})"
							content-width="137mm"
		            		content-height="100mm" />
	             </fo:block>	
	          </fo:table-cell>
	          	
             <fo:table-cell border="solid">
              <fo:block margin-left="0.1cm">
				<xsl:variable name="mapOrtho_url" select="/opdracht/bord/mapOrtho" />
<!-- 				<fo:external-graphic src="'{$mapOrtho_url}'" -->
<!-- 						content-width="137mm" -->
<!-- 	            		content-height="100mm" /> -->
					<fo:external-graphic src="url(file:/{$mapOrtho_url})"
							content-width="137mm"
		            		content-height="100mm" />
		      </fo:block>		
             </fo:table-cell>       
           </fo:table-row>
                
        </fo:table-body>
      </fo:table> 
		</fo:block>
  </xsl:template>
</xsl:stylesheet>