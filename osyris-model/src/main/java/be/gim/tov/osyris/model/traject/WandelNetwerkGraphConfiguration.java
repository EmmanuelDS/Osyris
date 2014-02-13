package be.gim.tov.osyris.model.traject;

import javax.enterprise.context.ApplicationScoped;

import be.gim.specto.api.topology.GraphConfiguration;

@ApplicationScoped
public class WandelNetwerkGraphConfiguration implements GraphConfiguration {

	@Override
	public String getNodeClassName() {
		return "WandelNetwerkKnooppunt";
	}

	@Override
	public String getNodeGeometryName() {
		return "geom";
	}

	@Override
	public String getEdgeClassName() {
		return "WandelNetwerkSegment";
	}

	@Override
	public String getEdgeGeometryName() {
		return "geom";
	}

	@Override
	public String getEdgeStartPropertyName() {
		return "vanKnooppunt";
	}

	@Override
	public String getEdgeEndPropertyName() {
		return "naarKnooppunt";
	}
}
