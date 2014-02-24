<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output method="xml" indent="yes" />

	<xsl:template match="/">
		<fo:root>
			<fo:layout-master-set>

			<fo:simple-page-master master-name="A4-landscape"
					page-height="21cm" page-width="29.7cm" margin="0.7cm">
					<fo:region-body />
				</fo:simple-page-master>				
			</fo:layout-master-set>

			<!-- OVERVIEW MAP A4 -->
			<fo:page-sequence master-reference="A4-landscape">
				<fo:flow flow-name="xsl-region-body">
					<xsl:call-template name="overviewMap1" />
				</fo:flow>
			</fo:page-sequence>
			<fo:page-sequence master-reference="A4-landscape">
				<fo:flow flow-name="xsl-region-body">
					<xsl:call-template name="overviewMap2" />
				</fo:flow>
			</fo:page-sequence>
			<fo:page-sequence master-reference="A4-landscape">
				<fo:flow flow-name="xsl-region-body">
					<xsl:call-template name="overviewMap3" />
				</fo:flow>
			</fo:page-sequence>
			<fo:page-sequence master-reference="A4-landscape">
				<fo:flow flow-name="xsl-region-body">
					<xsl:call-template name="overviewMap4" />
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<xsl:template name="overviewMap1">		
		<fo:table table-layout="auto">
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell>
						<fo:block text-align="center">
							<xsl:variable name="overviewMap_url1" select="/map/overviewMap1" />
							<fo:external-graphic src="'{$overviewMap_url1}'"/> 
						</fo:block>

					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template name="overviewMap2">	
		<fo:table table-layout="auto">
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell>
						<fo:block text-align="center">
							<xsl:variable name="overviewMap_url2" select="/map/overviewMap2" />
							<fo:external-graphic src="'{$overviewMap_url2}'"/>
						</fo:block>

					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template name="overviewMap3">		
		<fo:table table-layout="auto">
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell>
						<fo:block text-align="center">
							<xsl:variable name="overviewMap_url3" select="/map/overviewMap3" />
							<fo:external-graphic src="'{$overviewMap_url3}'"/>
						</fo:block>

					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template name="overviewMap4">		
		<fo:table table-layout="auto">
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell>
						<fo:block text-align="center">
							<xsl:variable name="overviewMap_url4" select="/map/overviewMap4" />
							<fo:external-graphic src="'{$overviewMap_url4}'"/>
						</fo:block>

					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
</xsl:stylesheet>