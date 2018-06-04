package org.dieschnittstelle.jee.esa.ser;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.dieschnittstelle.jee.esa.entities.crm.AbstractTouchpoint;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.dieschnittstelle.jee.esa.utils.Utils.show;

@WebServlet(urlPatterns = "/api/async/touchpoints", asyncSupported = true)
public class TouchpointServiceServletAsync extends HttpServlet {

	protected static Logger logger = Logger
			.getLogger(TouchpointServiceServletAsync.class);

	public TouchpointServiceServletAsync() {
		show("TouchpointServiceServlet: constructor invoked\n");
	}
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {

		logger.info("doGet()");

		AsyncContext asyncCtx = request.startAsync();
		RequestDispatcher dispatcher = asyncCtx.getRequest().getRequestDispatcher("/api/touchpoints");

		new Thread(()->{
			logger.info("doGet(): sleeping...");
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("doGet(): continuing...");
			try {
				dispatcher.forward(asyncCtx.getRequest(), asyncCtx.getResponse());
			}
			catch (Exception e) {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
		}).start();

	}
	

	@Override	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {

		// assume POST will only be used for touchpoint creation, i.e. there is
		// no need to check the uri that has been used

		// obtain the executor for reading out the touchpoints from the servlet context using the touchpointCRUD attribute
		TouchpointCRUDExecutor exec = (TouchpointCRUDExecutor) getServletContext().getAttribute("touchpointCRUD");

		try {
			// create an ObjectInputStream from the request's input stream
			ObjectInputStream ois = new ObjectInputStream(request.getInputStream());

			// read an AbstractTouchpoint object from the stream
			// call the create method on the executor and take its return value
			AbstractTouchpoint abstractTouchpoint = exec.createTouchpoint((AbstractTouchpoint) ois.readObject());

			// set the response status as successful, using the appropriate
			// constant from HttpServletResponse
			response.setStatus(HttpServletResponse.SC_OK);

			// then write the object to the response's output stream, using a
			// wrapping ObjectOutputStream
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);

			// write the object to the output stream
			oos.writeObject(abstractTouchpoint);
			oos.flush();
			byte[] objectBytes = baos.toByteArray();

			// ... and write the object to the stream
			response.getOutputStream().write(objectBytes);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
