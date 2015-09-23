package org.apache.chemistry.opencmis.couchbase.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.chemistry.opencmis.couchbase.CouchbaseService;

public class CouchbaseClusterServlet extends HttpServlet {

	private static final long serialVersionUID = -1268227021898028596L;
	CouchbaseService cbService ; 
	
	public CouchbaseClusterServlet() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(cbService != null) cbService.close();
		super.destroy();
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		cbService = CouchbaseService.getInstance();
	}

	@Override
	public void log(String msg) {
		// TODO Auto-generated method stub
		super.log(msg);
	}
	

}
