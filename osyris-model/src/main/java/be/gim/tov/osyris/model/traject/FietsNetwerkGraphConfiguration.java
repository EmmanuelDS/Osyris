package be.gim.tov.osyris.model.traject;

import javax.enterprise.context.ApplicationScoped;

import be.gim.specto.api.topology.GraphConfiguration;

@ApplicationScoped
public class FietsNetwerkGraphConfiguration implements GraphConfiguration {

	@Override
	public String getNodeClassName() {
		return "FietsNetwerkKnooppunt";
	}

	@Override
	public String getNodeGeometryName() {
		return "geom";
	}

	@Override
	public String getEdgeClassName() {
		return "FietsNetwerkSegment";
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
