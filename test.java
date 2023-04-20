import java.io.*;
public class Test implements Serializable {
    private int a;
    private int b;
    private boolean flag;

    public Test(int a, int b)
    {
        this.a = a;
        this.b = b;
        this.flag = true;
    }

    public int getA()
    {
        return this.a;
    }

    public void setA(int a)
    {
        this.a = a;
    }

    public int getB()
    {
        return this.b;
    }

    public void setB(int b)
    {
        this.b = b;
    }

    public void setflag(boolean flag)
    {
        this.flag = flag;
    }

    
}
