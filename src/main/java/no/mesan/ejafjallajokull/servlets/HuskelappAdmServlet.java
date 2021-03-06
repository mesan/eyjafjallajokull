package no.mesan.ejafjallajokull.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.mesan.ejafjallajokull.utils.ServletUtil;

/**
 * Servlet implementation class HuskelappAdmServlet
 */
public class HuskelappAdmServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public HuskelappAdmServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		// Sjekker om bruker har klikket p� sletteknapp for huskelapp
		boolean dbOperationsOk = false; // Holder rede p� om vi har en
										// feilsituasjon
		if (request.getParameter("sletthuskelapp") != null) {
			dbOperationsOk = slettHuskelapp(request.getParameter("tittel").toString(),
					request.getParameter("brukernavn").toString(), request.getParameter("timestamp").toString(),
					request, response);
		}
		// Bruker har laget ny huskelapp og trykket lagre, lagrer...
		else {
			String tittel = request.getParameter("tittel").toString();
			String innhold = request.getParameter("innhold").toString();
			int erOffentlig = ServletUtil.convertCheckBox(request.getParameter("erOffentlig"));
			dbOperationsOk = lagreHuskelapp(tittel, innhold, erOffentlig, request, response);
		}
		// Loginservlet henter ut oppdatert huskelappliste, forwarder.
		if (dbOperationsOk) {
			String nextJSP = "/login";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
			dispatcher.forward(request, response);
		}

	}
	

	/**
	 * Lagrer ny huskelapp for bruker.
	 * 
	 * @param tittel
	 * @param innhold
	 * @param erOffentlig
	 * @param request
	 * @param response
	 * @return
	 */
	private boolean lagreHuskelapp(String tittel, String innhold, int erOffentlig, HttpServletRequest request,
			HttpServletResponse response) {
		String sql = "INSERT INTO HUSKELAPP (TITTEL, INNHOLD, ERUTFORT, EROFFENTLIG, BRUKERNAVN, TIMESTAMP) VALUES ('"
				+ tittel + "', '" + innhold + "', 0," + erOffentlig + ", '"
				+ request.getSession().getAttribute("brukernavn").toString() + "', " + new java.util.Date().getTime()
				+ ")";
		System.out.println(sql);
		Connection con = null;;
		try {
			con = ServletUtil.initializeDBCon();		
			ServletUtil.beginTransaction(con);
			Statement stm = con.createStatement();
			stm.executeUpdate(sql);
			ServletUtil.endTransaction(con);
		} catch (Throwable e) {
			ServletUtil.rollbackTransaction(con);
			e.printStackTrace();
			request.setAttribute("feilmelding", "Kunne ikke sette inn huskelapp : " + e.getMessage());
			ServletUtil.gotoFeilSide(request, response);
			return false;
		}  finally {
			ServletUtil.cleanupTransaction(con);
			ServletUtil.cleanupDBConn(con);
		}
		return true;
	}

	/**
	 * Sletter en huskelapp fra systemet.
	 * 
	 * @param tittel
	 * @param brukernavn
	 * @param timestamp
	 * @param request
	 * @param response
	 */
	private boolean slettHuskelapp(String tittel, String brukernavn, String timestamp, HttpServletRequest request,
			HttpServletResponse response) {
		String SQL = "DELETE FROM HUSKELAPP WHERE TITTEL='" + tittel + "' AND brukernavn='" + brukernavn
				+ "' AND timestamp=" + new Long(timestamp).longValue();
		Connection con = null;
		try {
			con = ServletUtil.initializeDBCon();		
			ServletUtil.beginTransaction(con);
			Statement stm = con.createStatement();
			stm.executeUpdate(SQL);
			ServletUtil.endTransaction(con);
		} catch (Exception e) {
			ServletUtil.rollbackTransaction(con);
			request.setAttribute("feilmelding", "Kunne ikke slette huskelapp : " + e.getMessage());
			ServletUtil.gotoFeilSide(request, response);
			return false;
		} finally{
			ServletUtil.cleanupTransaction(con);
			ServletUtil.cleanupDBConn(con);
		}
		return true;
	}

}
