import java.util.ArrayList;
import java.util.regex.*;
//The Math engine which generates plottable data
public class Main_Process
{
    private ArrayList<String[]> rawPoints = new ArrayList<>();
    private ArrayList<String[]> IsoReadyPoints = new ArrayList<>();
    private ArrayList<Double[]> ReadyPoints3D = new ArrayList<>();
    private double radius;
    private Double[] Centre3D;
    private boolean dataReady;
    private boolean withCentre; int plane;
    Double[][] bridge; double iso_y, iso_x;
    public Main_Process()
    {
        Data_Distributor DD = Data_Distributor.getInstance();
        bridge = new Double[4][3];
    }
    public void iso_rect()
    {
        //Generates vertex points for rectangles based on input data
        IsoReadyPoints.clear(); ReadyPoints3D.clear();
        rawPoints = Data_Distributor.getInstance().getRawPoints();
        coord_split(); int i;
        String[] IsometricPoints = new String[5]; 
        Double[][] extraPt = new Double[2][3];
        
        if (bridge[0][0] == null || bridge[1][0] == null) return;
        if(bridge[0][0].equals(bridge[1][0]))
        {
            extraPt[0][0] = bridge[0][0]; extraPt[1][0] = bridge[0][0];
            extraPt[0][1] = bridge[0][1]; extraPt[1][1] = bridge[1][1];
            extraPt[0][2] = bridge[1][2]; extraPt[1][2] = bridge[0][2];
        }else if(bridge[0][1].equals(bridge[1][1]))
        {
            extraPt[0][0] = bridge[0][0]; extraPt[1][0] = bridge[1][0];
            extraPt[0][1] = bridge[0][1]; extraPt[1][1] = bridge[0][1];
            extraPt[0][2] = bridge[1][2]; extraPt[1][2] = bridge[0][2];
        }else if(bridge[0][2].equals(bridge[1][2]))
        {
            extraPt[0][0] = bridge[0][0]; extraPt[1][0] = bridge[1][0];
            extraPt[0][1] = bridge[1][1]; extraPt[1][1] = bridge[0][1];
            extraPt[0][2] = bridge[0][2]; extraPt[1][2] = bridge[0][2];
        } else 
        {
            for(i = 0; i < 3; i++) {
                extraPt[0][i] = bridge[0][i] + bridge[1][i] - bridge[2][i]; // P3 = P0 + P1 - P2
                extraPt[1][i] = bridge[2][i];
            }
        }int idx=0;
        
        ReadyPoints3D.add(new Double[]{bridge[0][0], bridge[0][1], bridge[0][2]}); // P0
        ReadyPoints3D.add(new Double[]{extraPt[0][0], extraPt[0][1], extraPt[0][2]}); // P2
        ReadyPoints3D.add(new Double[]{bridge[1][0], bridge[1][1], bridge[1][2]}); // P1
        ReadyPoints3D.add(new Double[]{extraPt[1][0], extraPt[1][1], extraPt[1][2]}); // P3
        
        //Generation of Isometric coordinates
        for(i = 0; i < bridge.length; i++) 
        {
            if(bridge[i][0] != null) 
            {
                iso_x = Math.cos(Math.toRadians(30))*(bridge[i][0] - bridge[i][1]);
                iso_y = 0.5*(bridge[i][0] + bridge[i][1]) + bridge[i][2];
                String storer = Double.toString(iso_x) + " " + Double.toString(iso_y);
                IsometricPoints[idx] = storer; idx++;
            }
        }
        
        for(int j = 0; j < 2; j++) 
        {
            if(extraPt[j][0] != null) 
            {
                iso_x = Math.cos(Math.toRadians(30))*(extraPt[j][0] - extraPt[j][1]);
                iso_y = 0.5*(extraPt[j][0] + extraPt[j][1]) + extraPt[j][2];
                String storer = Double.toString(iso_x) + " " + Double.toString(iso_y);
                IsometricPoints[idx] = storer; idx++;
            }
        }
        
        IsoReadyPoints.add(IsometricPoints);
        Data_Distributor.getInstance().setProcessedData(IsoReadyPoints);
        Data_Distributor.getInstance().set3DPoints(ReadyPoints3D);
        Plotter.getInstance().RectLinePlot(); rawPoints.clear();
    }
    public void iso_circle()
    { 
        //This method generates circumference points of a circle based on selected condition
        IsoReadyPoints.clear(); Centre3D = new Double[3];
        rawPoints = Data_Distributor.getInstance().getRawPoints();
        coord_split(); int i; int totalPoints = 0;
        plane = Data_Distributor.getInstance().getPlane(); 
        ArrayList<Double[]> circlePoints = new ArrayList<>(); 
        
        for(i = 0; i < 3; i++) {
            Centre3D[i] = bridge[0][i];
        }
        if (bridge[0][0] == null) return;

        if(plane == 1)
        {   
            radius = Data_Distributor.getInstance().getD();
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20); 
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            for(i=0;i<=totalPoints;i++) 
            {
                Double[] pts = new Double[3];
                double angle = 2*Math.PI*i/totalPoints;
                pts[0] = bridge[0][0] + radius*Math.cos(angle);
                pts[1] = bridge[0][1] + radius*Math.sin(angle);
                pts[2] = bridge[0][2];
                circlePoints.add(pts);
            }
        }
        else if(plane == 2)
        {   
            radius = Data_Distributor.getInstance().getD(); 
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20);
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            for(i=0;i<=totalPoints;i++) 
            {
                Double[] pts = new Double[3];
                double angle = 2*Math.PI*i/totalPoints;
                pts[0] = bridge[0][0] ;
                pts[1] = bridge[0][1] + radius*Math.cos(angle);
                pts[2] = bridge[0][2] + radius*Math.sin(angle);
                circlePoints.add(pts);
            }
        }
        else if(plane == 3)
        {   
            radius = Data_Distributor.getInstance().getD(); 
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20);
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            for(i=0;i<=totalPoints;i++) 
            {
                Double[] pts = new Double[3];
                double angle = 2*Math.PI*i/totalPoints;
                pts[0] = bridge[0][0] + radius*Math.cos(angle);
                pts[1] = bridge[0][1] ;
                pts[2] = bridge[0][2] + radius*Math.sin(angle);
                circlePoints.add(pts);
            }
        }
        else if(plane == 0)
        {
            /*Calculates the local axes of the circle's plane and finds points 
            via the vector parametric equation*/  
            double[] u = new double[3]; double[] v = new double[3]; 
            double[] n = new double[3];
            for(i=0;i<3;i++) {
                u[i] = bridge[1][i] - bridge[0][i]; v[i] = bridge[2][i] - bridge[0][i];
            }
            
            double magU = u[0]*u[0] + u[1]*u[1] + u[2]*u[2];
            double magV = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
            if(Math.abs(magU - magV) > 0.001) {    
                UserMessenger.show(ErrorCode.POINTS_NOT_ON_CIRCLE);
                return;
            }
                
            radius = Math.sqrt(magU);
            n[0] = u[1]*v[2] - v[1]*u[2];
            n[1] = u[2]*v[0] - u[0]*v[2];
            n[2] = u[0]*v[1] - u[1]*v[0];
            double mag = Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
            n[0] = n[0]/mag; n[1] = n[1]/mag; n[2] = n[2]/mag;
            mag = Math.sqrt(u[0]*u[0] + u[1]*u[1] + u[2]*u[2]);
           
            u[0] = u[0]/mag; u[1] = u[1]/mag; u[2] = u[2]/mag;
            v[0] = u[1]*n[2] - u[2]*n[1];
            v[1] = u[2]*n[0] - u[0]*n[2];
            v[2] = u[0]*n[1] - u[1]*n[0];
            mag = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            v[0] = v[0]/mag; v[1] = v[1]/mag; v[2] = v[2]/mag;
           
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20);
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            for(i=0;i<=totalPoints;i++) 
            {
                Double[] pts = new Double[3];
                double theta = 2*Math.PI*i/totalPoints;
                double cosT = Math.cos(theta);
                double sinT = Math.sin(theta);
                for(int j=0;j<3;j++){
                    pts[j] = bridge[0][j] + radius*(u[j]*cosT + v[j]*sinT);}
                circlePoints.add(pts);
            }
        }
        else if(plane == 4)
        {
            /*Finds the normal to the plane at the centre, then finds points via
            the vector parametric equation*/
            double[] u = new double[3]; double[] v = new double[3]; 
            double[] n = new double[3]; double[] m12 = new double[3];
            double[] m23 = new double[3]; double[] d1 = new double[3];
            double[] d2 = new double[3]; double[] c = new double[3];
            
            for(i = 0;i<3;i++){
                m12[i] = (bridge[0][i] + bridge[1][i])/2;
                m23[i] = (bridge[2][i] + bridge[1][i])/2;
            }
            for(i=0;i<3;i++) {
                u[i] = bridge[1][i] - bridge[0][i]; 
                v[i] = bridge[2][i] - bridge[1][i];
            }
            
            n[0] = u[1]*v[2] - u[2]*v[1];
            n[1] = u[2]*v[0] - u[0]*v[2];
            n[2] = u[0]*v[1] - u[1]*v[0];
            
            double isoUp = -n[0] * 0.866 + -n[1] * 0.866 + n[2];
            if (isoUp < 0) {
                n[0] = -n[0];
                n[1] = -n[1];
                n[2] = -n[2];
            }
            
            d1[0] = n[1]*u[2] - n[2]*u[1];
            d1[1] = n[2]*u[0] - n[0]*u[2];
            d1[2] = n[0]*u[1] - n[1]*u[0];
            d2[0] = n[1]*v[2] - n[2]*v[1];
            d2[1] = n[2]*v[0] - n[0]*v[2];
            d2[2] = n[0]*v[1] - n[1]*v[0];
            
            double a11 = d1[0]*d1[0] + d1[1]*d1[1] + d1[2]*d1[2];
            double a12 = -(d1[0]*d2[0] + d1[1]*d2[1] + d1[2]*d2[2]);
            double a21 = a12;
            double a22 = d2[0]*d2[0] + d2[1]*d2[1] + d2[2]*d2[2];
            double diff0 = m23[0] - m12[0];
            double diff1 = m23[1] - m12[1];
            double diff2 = m23[2] - m12[2];

            double b1 = d1[0]*diff0 + d1[1]*diff1 + d1[2]*diff2;
            double b2 = -(d2[0]*diff0 + d2[1]*diff1 + d2[2]*diff2);
            double det = a11*a22 - a12*a21;
            if (Math.abs(det) < 0.0001) {
                UserMessenger.show(ErrorCode.POINTS_COLLINEAR);
                return;
            }

            double t1 = (b1*a22 - b2*a12) / det;
            for(i=0;i<3;i++) {
                c[i] = m12[i] + t1*d1[i];
                Centre3D[i] = c[i];
            }
            for(i=0;i<3;i++) {
                u[i] = bridge[0][i] - c[i]; 
            }
            
            radius = Math.sqrt(u[0]*u[0] + u[1]*u[1] + u[2]*u[2]);
            double mag = Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
            n[0] = n[0]/mag; n[1] = n[1]/mag; n[2] = n[2]/mag;
            mag = Math.sqrt(u[0]*u[0] + u[1]*u[1] + u[2]*u[2]);
            u[0] = u[0]/mag; u[1] = u[1]/mag; u[2] = u[2]/mag;
            v[0] = u[1]*n[2] - u[2]*n[1];
            v[1] = u[2]*n[0] - u[0]*n[2];
            v[2] = u[0]*n[1] - u[1]*n[0];
            mag = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            v[0] = v[0]/mag; v[1] = v[1]/mag; v[2] = v[2]/mag;
            
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20);
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            for(i=0;i<=totalPoints;i++) 
            {
                Double[] pts = new Double[3];
                double theta = 2*Math.PI*i/totalPoints;
                double cosT = Math.cos(theta);
                double sinT = Math.sin(theta);
                for(int j=0;j<3;j++){
                    pts[j] = c[j] + radius*(u[j]*cosT + v[j]*sinT);}
                circlePoints.add(pts);
            }
        }   i = 0;
    
        String[] IsometricPoints = new String[totalPoints + 5];
        for(Double[] P : circlePoints)
        {
            iso_x = Math.cos(Math.toRadians(30)) * (P[0] - P[1]);
            iso_y = 0.5*(P[0] + P[1]) + P[2];
            String storer = Double.toString(iso_x) + " " + Double.toString(iso_y);
            IsometricPoints[i] = storer; i++;
        }
        
        IsoReadyPoints.add(IsometricPoints);
        Data_Distributor.getInstance().setProcessedData(IsoReadyPoints);
        Data_Distributor.getInstance().setCentre3D(Centre3D);
        Plotter.getInstance().CirclePlot(); rawPoints.clear();
    }
    public void iso_line()
    {
        //Finds end points of line segments in isometric coordinates
        IsoReadyPoints.clear(); ReadyPoints3D.clear();
        rawPoints = Data_Distributor.getInstance().getRawPoints();
        coord_split(); String[] IsometricPoints = new String[2];
        if (bridge[0][0] == null || bridge[1][0] == null) return;
        
        for (int i = 0; i < bridge.length; i++) 
        {
            if (bridge[i][0] != null) {
                ReadyPoints3D.add(new Double[]{bridge[i][0], bridge[i][1], bridge[i][2]});
            }
        }
        
        for(int i=0;i<2;i++)
        {
            iso_x = Math.cos(Math.toRadians(30))*(bridge[i][0] - bridge[i][1]);
            iso_y = 0.5*(bridge[i][0] + bridge[i][1]) + bridge[i][2];
            String storer = Double.toString(iso_x) + " " + Double.toString(iso_y);
            IsometricPoints[i] = storer;
        }
        
        IsoReadyPoints.add(IsometricPoints);
        Data_Distributor.getInstance().setProcessedData(IsoReadyPoints);
        Data_Distributor.getInstance().set3DPoints(ReadyPoints3D);
        Plotter.getInstance().LinePlot(); rawPoints.clear();
    }
    public void iso_arc()
    {
        //Calculates points on a circle between two given points
        IsoReadyPoints.clear(); Centre3D = new Double[3];
        rawPoints = Data_Distributor.getInstance().getRawPoints();
        coord_split(); int i;int totalPoints = 0;
        withCentre = Data_Distributor.getInstance().getCentC();
        ArrayList<Double[]> circlePoints = new ArrayList<>();
        if (bridge[0][0] == null || bridge[1][0] == null) return;
        
        if(withCentre)
        {
            double[] u = new double[3]; double[] v = new double[3]; 
            double[] n = new double[3];
            for(i = 0; i < 3; i++) {
                Centre3D[i] = bridge[0][i];
            }
            for(i=0;i<3;i++) {
                u[i] = bridge[1][i] - bridge[0][i]; 
                v[i] = bridge[2][i] - bridge[0][i];
            }
            
            double magU = u[0]*u[0] + u[1]*u[1] + u[2]*u[2];
            double magV = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
            if(Math.abs(magU - magV) > 0.01){    
                UserMessenger.show(ErrorCode.POINTS_NOT_ON_CIRCLE);
                return;
            }
                
            radius = Math.sqrt(magU);
            n[0] = u[1]*v[2] - v[1]*u[2];
            n[1] = u[2]*v[0] - u[0]*v[2];
            n[2] = u[0]*v[1] - u[1]*v[0];
            
            double isoUp = -n[0] * 0.866 + -n[1] * 0.866 + n[2];
            if (isoUp < 0) {
                n[0] = -n[0];
                n[1] = -n[1];
                n[2] = -n[2];
            }
            
            double mag = Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
            n[0] = n[0]/mag; n[1] = n[1]/mag; n[2] = n[2]/mag;
            mag = Math.sqrt(u[0]*u[0] + u[1]*u[1] + u[2]*u[2]);
            u[0] = u[0]/mag; u[1] = u[1]/mag; u[2] = u[2]/mag;
            v[0] = u[1]*n[2] - u[2]*n[1];
            v[1] = u[2]*n[0] - u[0]*n[2];
            v[2] = u[0]*n[1] - u[1]*n[0];
            
            mag = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            v[0] = v[0]/mag; v[1] = v[1]/mag; v[2] = v[2]/mag;
            double v2du = 0.0, v2dv = 0.0;
            
            for(int j=0;j<3;j++){
                v2du += (bridge[2][j] - bridge[0][j])*u[j];
                v2dv += (bridge[2][j] - bridge[0][j])*v[j];
            }
            
            if (v2dv < 0) {
                v[0] = -v[0]; v[1] = -v[1]; v[2] = -v[2];
            }//Finds the angle of the sector, preserving the quadrant information
            double A = Math.atan2(v2dv, v2du);
            if(A<0) { A += 2*Math.PI;}
            if (A > Math.PI) {
                A = 2 * Math.PI - A;  
            }
            
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20);
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            int count = (int)((A*totalPoints)/(Math.PI*2));

            for (i = 0; i <= count; i++) 
            {
                Double[] pts = new Double[3];
                double theta = A * i / count;
                double cosT = Math.cos(theta);
                double sinT = Math.sin(theta);
                for (int j = 0; j < 3; j++) {
                    pts[j] = bridge[0][j] + radius*(u[j]*cosT + v[j]*sinT);
                }
                circlePoints.add(pts);
            }
        }
        else
        {
            /*Using 3 points on a circle, finds the centre, then finds the local
            axes, the angle of sector, calculating all points in the end*/
            double[] u = new double[3]; double[] v = new double[3]; 
            double[] n = new double[3]; double[] m12 = new double[3];
            double[] m23 = new double[3]; double[] d1 = new double[3];
            double[] d2 = new double[3]; double[] c = new double[3];
            
            for(i = 0;i<3;i++){
                m12[i] = (bridge[0][i] + bridge[1][i])/2;
                m23[i] = (bridge[2][i] + bridge[1][i])/2;
            }
            for(i=0;i<3;i++) {
                u[i] = bridge[1][i] - bridge[0][i]; 
                v[i] = bridge[2][i] - bridge[1][i];
            }
            
            n[0] = u[1]*v[2] - u[2]*v[1];
            n[1] = u[2]*v[0] - u[0]*v[2];
            n[2] = u[0]*v[1] - u[1]*v[0];
            
            //To standardise direction of normal to circle's plane
            double isoUp = -n[0] * 0.866 + -n[1] * 0.866 + n[2];
            if (isoUp < 0) {
                n[0] = -n[0];
                n[1] = -n[1];
                n[2] = -n[2];
            }
            
            d1[0] = n[1]*u[2] - n[2]*u[1];
            d1[1] = n[2]*u[0] - n[0]*u[2];
            d1[2] = n[0]*u[1] - n[1]*u[0];
            d2[0] = n[1]*v[2] - n[2]*v[1];
            d2[1] = n[2]*v[0] - n[0]*v[2];
            d2[2] = n[0]*v[1] - n[1]*v[0];
            
            double a11 = d1[0]*d1[0] + d1[1]*d1[1] + d1[2]*d1[2];
            double a12 = -(d1[0]*d2[0] + d1[1]*d2[1] + d1[2]*d2[2]);
            double a21 = a12;
            double a22 = d2[0]*d2[0] + d2[1]*d2[1] + d2[2]*d2[2];
            double diff0 = m23[0] - m12[0];
            double diff1 = m23[1] - m12[1];
            double diff2 = m23[2] - m12[2];

            double b1 = d1[0]*diff0 + d1[1]*diff1 + d1[2]*diff2;
            double b2 = -(d2[0]*diff0 + d2[1]*diff1 + d2[2]*diff2);
            double det = a11*a22 - a12*a21;
            if (Math.abs(det) < 0.0001) {
                UserMessenger.show(ErrorCode.POINTS_COLLINEAR);
                return;
            }
            double t1 = (b1*a22 - b2*a12) / det;
            
            for (i=0;i<3;i++) {
                c[i] = m12[i] + t1*d1[i];
                Centre3D[i] = c[i];
                }
            for(i=0;i<3;i++) {
                u[i] = bridge[0][i] - c[i]; 
            }
            
            radius = Math.sqrt(u[0]*u[0] + u[1]*u[1] + u[2]*u[2]);
            double mag = Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
            n[0] = n[0]/mag; n[1] = n[1]/mag; n[2] = n[2]/mag;
            mag = Math.sqrt(u[0]*u[0] + u[1]*u[1] + u[2]*u[2]);
            u[0] = u[0]/mag; u[1] = u[1]/mag; u[2] = u[2]/mag;
            v[0] = u[1]*n[2] - u[2]*n[1];
            v[1] = u[2]*n[0] - u[0]*n[2];
            v[2] = u[0]*n[1] - u[1]*n[0];
            mag = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            v[0] = v[0]/mag; v[1] = v[1]/mag; v[2] = v[2]/mag;
            double v2du = 0.0, v2dv = 0.0;
            
            for(int j=0;j<3;j++){
                v2du += (bridge[1][j] - c[j])*u[j];
                v2dv += (bridge[1][j] - c[j])*v[j];
            }
            
            double A2 = Math.atan2(v2dv, v2du);
            v2du = 0.0; v2dv = 0.0;
            
            for(int j=0;j<3;j++){
                v2du += (bridge[2][j] - c[j])*u[j];
                v2dv += (bridge[2][j] - c[j])*v[j];
            }
            //To standardise direction of v wrt u
            if (v2dv < 0) {
                v[0] = -v[0]; v[1] = -v[1]; v[2] = -v[2];
            }
            
            double A3 = Math.atan2(v2dv, v2du);
            if(A2<0){A2 += 2*Math.PI;}
            if(A3<0){A3 += 2*Math.PI;}
            if (A2 > A3) {
                double temp = A2; A2 = A3; A3 = temp;
            }
            if (A3 > Math.PI) {
                A3 = 2 * Math.PI - A3; 
            }
            
            double circumference = 2 * Math.PI * radius;
            totalPoints = (int) (circumference * 20);
            totalPoints = Math.max(180, Math.min(totalPoints, 720));
            int count = (int)((A3*totalPoints)/(Math.PI*2));
            
            for(i=0; i<count; i++) 
            {
                Double[] pts = new Double[3];
                double theta = A3 * i / count;
                double cosT = Math.cos(theta);
                double sinT = Math.sin(theta);
                for (int j = 0; j < 3; j++) {
                    pts[j] = c[j] + radius*(u[j]*cosT + v[j]*sinT);
                }
                circlePoints.add(pts);
            }
        }
        i = 0;
        String[] IsometricPoints = new String[totalPoints + 5];
        for(Double[] P : circlePoints)
        {
            iso_x = Math.cos(Math.toRadians(30)) * (P[0] - P[1]);
            iso_y = 0.5*(P[0] + P[1]) + P[2];
            String storer = Double.toString(iso_x) + " " + Double.toString(iso_y);
            IsometricPoints[i] = storer; i++;
        }
        
        IsoReadyPoints.add(IsometricPoints);
        Data_Distributor.getInstance().setProcessedData(IsoReadyPoints);
        Data_Distributor.getInstance().setCentre3D(Centre3D);
        Plotter.getInstance().ArcPlot(); rawPoints.clear();
    }
    public void coord_split()
    {
        //Splitting parsed 3D coordinates
        Pattern pat = Pattern.compile("(\\d+\\.?\\d*)\\,(\\d+\\.?\\d*)\\,(\\d+\\.?\\d*)");
        int row = 0;
        for(String[] point : rawPoints) 
        {
            for(String pt : point) 
            {
                if (pt == null) {continue;}
                Matcher mat = pat.matcher(pt);
                if (mat.matches() && row < 3) 
                {
                    bridge[row][0] = Double.parseDouble(mat.group(1));
                    bridge[row][1] = Double.parseDouble(mat.group(2));
                    bridge[row][2] = Double.parseDouble(mat.group(3));
                    row++;
                }
                if (!mat.matches() && row == 0) 
                {
                    UserMessenger.show(ErrorCode.INVALID_COORDINATE_FORMAT);
                    return;
                }
            }
        }
    }
}