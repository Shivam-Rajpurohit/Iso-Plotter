import java.util.ArrayList;
//This class accepts and distributes input and processed data as a Singleton
public class Data_Distributor 
{
    private static Data_Distributor instance;
    private ArrayList<String[]> rawPoints = new ArrayList<>();
    private ArrayList<String[]> IsoReadyPoints = new ArrayList<>();
    private ArrayList<Double[]> ReadyPoints3D = new ArrayList<>();
    private Double Dist;
    private Double[] centre3D;
    private boolean dataReady = false;
    private boolean with_centre = false;
    private int Pl;
    private Data_Distributor() {}  
    public static synchronized Data_Distributor getInstance() 
    {
        if (instance == null) {
            instance = new Data_Distributor();
        }   return instance;
    }
    public synchronized void addRawData(String a, String b, String c, Double d)
    {
        if (a != null) {
            rawPoints.add(new String[]{a, b, c});
            dataReady = false; Dist = d;}
    }
    public synchronized void Cent_cond(boolean cc)
    {   
        this.with_centre = cc;  dataReady = false; 
    }
    public synchronized void Plane(int i)
    {   
        this.Pl = i;    dataReady = false;
    }
    public synchronized void setProcessedData(ArrayList<String[]> processed) 
    {
        if(processed != null){
            this.IsoReadyPoints = new ArrayList<>();
            for (String[] point : processed) {
                this.IsoReadyPoints.add(point.clone());
            }   this.dataReady = true;
        } else { this.dataReady = false;}
    }
    public synchronized void set3DPoints(ArrayList<Double[]> Points3D)
    {
        if(Points3D != null){
            this.ReadyPoints3D = new ArrayList<>();
            for(Double[] point : Points3D){
                this.ReadyPoints3D.add(point.clone());
            }   
        }
    }
    public synchronized void setCentre3D(Double[] c) {
        this.centre3D = c != null ? c.clone() : null;
    }   
    public synchronized ArrayList<String[]> getPlotPoints() 
    {
        if (!this.dataReady) {return new ArrayList<>();}
        ArrayList<String[]> copy = new ArrayList<>();
        for (String[] point : IsoReadyPoints){copy.add(point.clone());}
        return copy;
    }
    public synchronized ArrayList<Double[]> get3DPoints() 
    {
        if (!this.dataReady) {return new ArrayList<>();}
        ArrayList<Double[]> copy = new ArrayList<>();
        for (Double[] point : ReadyPoints3D){copy.add(point.clone());}
        return copy;
    }
    public synchronized Double[] getCentre3D() 
    { 
        return centre3D != null ? centre3D.clone() : null;
    }
    public synchronized double getD() { return Dist; }
    public synchronized boolean getCentC() { return with_centre;}
    public synchronized int getPlane(){ return Pl;}
    public synchronized ArrayList<String[]> getRawPoints() 
    {
        ArrayList<String[]> copy = new ArrayList<>();
        for (String[] point : rawPoints) {
            copy.add(point.clone());
        }   return copy;
    }
    public synchronized void clear() 
    {
        rawPoints.clear();   Pl = 5;    centre3D = null;
        Dist = 0.0;   dataReady = false;   with_centre = false;
        IsoReadyPoints.clear();    ReadyPoints3D.clear();
    }
}