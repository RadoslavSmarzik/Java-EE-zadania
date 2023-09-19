package ee.servletmnohouholnik;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet(name = "Polygon", value = "/Polygon")
public class PolygonServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/png");
        String polygonString = request.getParameter("coords");

        OutputStream outputStream = response.getOutputStream();
        Polygon polygon = createPolygon(polygonString);
        double areaOfPolygon = calculateAreaOfPolygon(polygon);
        drawPolygon(polygon, areaOfPolygon,outputStream);
    }

    double calculateAreaOfPolygon(Polygon polygon){
        int n = polygon.npoints;
        int[] X = polygon.xpoints;
        int[] Y = polygon.ypoints;

        // Initialize area
        double area = 0.0;

        // Calculate value of shoelace formula
        int j = n - 1;
        for (int i = 0; i < n; i++)
        {
            area += (X[j] + X[i]) * (Y[j] - Y[i]);

            // j is previous vertex to i
            j = i;
        }

        // Return absolute value
        return Math.abs(area / 2.0);

    }

     Polygon createPolygon(String coordsString){
        String[] pointsString = coordsString.split(";");
        int numberOfPoints = pointsString.length;
        int[] x = new int[numberOfPoints];
        int[] y = new int[numberOfPoints];
        for(int i = 0; i < numberOfPoints; i++){
            String[] point = pointsString[i].split(" ");
            x[i] = Integer.parseInt(point[0]);
            y[i] = Integer.parseInt(point[1]);
        }
        Polygon p = new Polygon(x,y,numberOfPoints);
        return p;
    }

    void drawPolygon(Polygon polygon, double areaOfPolygon, OutputStream outputStream) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(600, 700,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 600, 700);

        g.setColor(Color.BLACK);
        g.drawPolygon(polygon);

        g.drawString("Plocha mnohouholníka je " + areaOfPolygon, 100, 650);

        g.dispose();

        ImageIO.write(bufferedImage, "png", outputStream);
        outputStream.close();

    }

}
