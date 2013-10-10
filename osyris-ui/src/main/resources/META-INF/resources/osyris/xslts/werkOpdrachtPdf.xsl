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

		 <xsl:call-template name="opdrachtFiche" />
		 
		 <xsl:if test="//@bordFiche='true'">
			<xsl:call-template name="bordFiche" />
		 </xsl:if>         
        
        </fo:flow>
      </fo:page-sequence>    
    </fo:root>
  </xsl:template>
  
  <xsl:template name="opdrachtFiche">
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
        <fo:inline font-weight="bold"><xsl:text>Route: </xsl:text></fo:inline>
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
			     select="/opdracht/bord/bordStraat">
			</xsl:value-of>
        </fo:block>
        
         <fo:block margin-left="30px">
         <fo:inline font-weight="bold"><xsl:text>Paaltype: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/bord/paaltype">
          	</xsl:value-of>
	     </fo:block>
	     <fo:block margin-left="30px">
	     <fo:inline font-weight="bold"><xsl:text>Paaldiameter: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/bord/paaldiameter">
          	</xsl:value-of>
          </fo:block>
          <fo:block margin-left="30px">
          <fo:inline font-weight="bold"><xsl:text>Paalbeugel: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/bord/paalbeugel">
          	</xsl:value-of>
          </fo:block>
		  <fo:block margin-left="30px" margin-bottom="20px">
		  <fo:inline font-weight="bold"><xsl:text>Paalondergrond: </xsl:text></fo:inline>
          	<xsl:value-of
          		select="/opdracht/bord/paalondergrond">
          	</xsl:value-of>
          </fo:block>	
        </xsl:if> 
        
        <fo:block margin-left="30px" margin-bottom="20px">
         	<fo:inline font-weight="bold"><xsl:text>Opdracht: </xsl:text></fo:inline>
        </fo:block>
         
         <xsl:for-each select="/opdracht/handeling">
          <fo:block margin-left="60px" margin-bottom="20px">
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
  </xsl:template>
  
  <xsl:template name="bordFiche">
  		<fo:block page-break-before="always"/> 
  		<fo:block margin-bottom="5px">
        <fo:inline font-weight="bold"><xsl:text>Opdrachtnummer: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/id">
			</xsl:value-of>
        </fo:block>
        <fo:block margin-bottom="5px">
        <fo:inline font-weight="bold"><xsl:text>Opdracht: </xsl:text></fo:inline>
          	<xsl:value-of
			     select="/opdracht/bordHandeling">
			</xsl:value-of>
        </fo:block>

		<fo:block>
		<fo:table border="solid" table-layout="fixed" width="100%" height="70%">
        <fo:table-header border="solid">
        	<fo:table-row>
                    <fo:table-cell border="solid">
                        <fo:block text-align="center" font-weight="bold">TOERISME OOST-VLAANDEREN</fo:block>
                        <fo:block text-align="center" font-weight="bold">Fiche 
	                        <fo:inline color="red">
		                        <xsl:value-of
				          			select="/opdracht/trajecttype">	
				          		</xsl:value-of>
				          	</fo:inline> 
			          	</fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block text-align="center" font-weight="bold">REGIO: 
                       	<xsl:value-of
	          				select="/opdracht/regio">
		          		</xsl:value-of>
                        </fo:block>
                    </fo:table-cell>
           </fo:table-row>
        </fo:table-header>
        
        <fo:table-body>    
          <fo:table-row height="60mm" >
            <fo:table-cell border="solid" >
            
             <fo:block-container>												
             	<fo:block>
            		<fo:external-graphic src="http://osyristest.tov.be/fotos/geen%20foto.png" 
            		content-width="50mm"
            		content-height="60mm" />
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
	          
				<xsl:if test="//@netwerkbord='true'">
					  <fo:block space-after="8pt">
					  <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 0: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt0">
			          	</xsl:value-of>
			          </fo:block>
			          <fo:block space-after="8pt">
			          <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 1: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt1">
			          	</xsl:value-of>
			          </fo:block>
			           <fo:block space-after="8pt">
			           <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 2: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt2">
			          	</xsl:value-of>
			          </fo:block>
			           <fo:block space-after="8pt">
			           <fo:inline font-weight="bold"><xsl:text>KnooppuntNr 3: </xsl:text></fo:inline>
			          	<xsl:value-of
			          		select="/opdracht/bord/knooppunt3">
			          	</xsl:value-of>
			          </fo:block>
				</xsl:if>				
              </fo:block>
             </fo:block-container>
            </fo:table-cell>
            
            
            <fo:table-cell>
            <fo:block-container>
             <xsl:variable name="url" select="/opdracht/bord/foto" />													
             	<fo:block margin-left="0.1cm">
            		<fo:external-graphic src="'{$url}'" 
            		content-width="50mm"
            		content-height="60mm" />
       			 </fo:block>
             </fo:block-container>
           
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
	          </fo:block>	
	          </fo:block-container>			
            </fo:table-cell>
           </fo:table-row>
              
           <fo:table-row height="80mm">
            <fo:table-cell border="solid">
            	 <fo:block margin-left="0.1cm">
					<xsl:variable name="mapTopo_url" select="/opdracht/bord/mapTopo" />
					<fo:external-graphic src="'{$mapTopo_url}'"
							content-width="137mm"
		            		content-height="100mm" />
	             </fo:block>	
	          </fo:table-cell>
	          	
             <fo:table-cell border="solid">
              <fo:block margin-left="0.1cm">
				<xsl:variable name="mapOrtho_url" select="/opdracht/bord/mapOrtho" />
				<fo:external-graphic src="'{$mapOrtho_url}'"
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