package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;

/**
 * Created by Bart on 03/05/2015.
 */
public class PNGtoBox2D {
        // tolerance is the amount of alpha for a pixel to be considered solid
        private float tolerance = 0.99f;
        public Pixmap result;

        public FloatArray marchingSquares(Pixmap pixmap){
            FloatArray contourVector = new FloatArray();

            // Debug
            result = new Pixmap(pixmap.getWidth(),pixmap.getHeight(),pixmap.getFormat());
            Color c = new Color();
            c.r = 0.f;
            c.g = 0.f;
            c.b = 0.f;
            c.a = 0.f;
            result.setColor(c);
            result.fill();
            c.r = 0.f;
            c.g = 1.f;
            c.b = 0.f;
            c.a = 1.f;

            Vector2 startPoint = getStartingPixel(pixmap);

            // if we found a starting pixel we can begin
            if (startPoint!=null) {

                //Starting point
                int pX = (int)(startPoint.x);
                int pY = (int)(startPoint.y);
                //TODO : Check px py > 0

                // stepX and stepY can be -1, 0 or 1 and represent the step in pixels to reach
                // next contour point
                int stepX;
                int stepY;
                // we also need to save the previous step, that's why we use prevX and prevY
                int prevX = 3;
                int prevY = 3;
                // closedLoop will be true once we traced the full contour
                boolean closedLoop = false;
                int count = 0;

                while (!closedLoop) {
                    // get alpha of all four pixel
                    Color temp = new Color();
                    temp.rgba8888ToColor(temp,pixmap.getPixel(pX-1,pY-1));
                    float alpha11 = temp.a;
                    temp.rgba8888ToColor(temp,pixmap.getPixel(pX,pY-1));
                    float alpha12 = temp.a;
                    temp.rgba8888ToColor(temp,pixmap.getPixel(pX-1,pY));
                    float alpha21 = temp.a;
                    temp.rgba8888ToColor(temp,pixmap.getPixel(pX,pY));
                    float alpha22 = temp.a;

                    int squareValue = getSquareValue(alpha11,alpha12,alpha21,alpha22);
                    switch (squareValue) {
							/* going UP with these cases:

							+---+---+   +---+---+   +---+---+
							| 1 |   |   | 1 |   |   | 1 |   |
							+---+---+   +---+---+   +---+---+
							|   |   |   | 4 |   |   | 4 | 8 |
							+---+---+  	+---+---+  	+---+---+

							*/
                        case 1 :
                        case 5 :
                        case 13 :
                            stepX=0;
                            stepY=-1;
                            break;
							/* going DOWN with these cases:

							+---+---+   +---+---+   +---+---+
							|   |   |   |   | 2 |   | 1 | 2 |
							+---+---+   +---+---+   +---+---+
							|   | 8 |   |   | 8 |   |   | 8 |
							+---+---+  	+---+---+  	+---+---+

							*/
                        case 8 :
                        case 10 :
                        case 11 :
                            stepX=0;
                            stepY=1;
                            break;
							/* going LEFT with these cases:

							+---+---+   +---+---+   +---+---+
							|   |   |   |   |   |   |   | 2 |
							+---+---+   +---+---+   +---+---+
							| 4 |   |   | 4 | 8 |   | 4 | 8 |
							+---+---+  	+---+---+  	+---+---+

							*/
                        case 4 :
                        case 12 :
                        case 14 :
                            stepX=-1;
                            stepY=0;
                            break;
							/* going RIGHT with these cases:

							+---+---+   +---+---+   +---+---+
							|   | 2 |   | 1 | 2 |   | 1 | 2 |
							+---+---+   +---+---+   +---+---+
							|   |   |   |   |   |   | 4 |   |
							+---+---+  	+---+---+  	+---+---+

							*/
                        case 2 :
                        case 3 :
                        case 7 :
                            stepX=1;
                            stepY=0;
                            break;
                        case 6 :
							/* special saddle point case 1:

							+---+---+
							|   | 2 |
							+---+---+
							| 4 |   |
							+---+---+

							going LEFT if coming from UP
							else going RIGHT

							*/
                            if (prevX==0&&prevY==-1) {
                                stepX=-1;
                                stepY=0;
                            }
                            else {
                                stepX=1;
                                stepY=0;
                            }
                            break;
                        case 9 :
						/* special saddle point case 2:

							+---+---+
							| 1 |   |
							+---+---+
							|   | 8 |
							+---+---+

							going UP if coming from RIGHT
							else going DOWN

							*/
                            if (prevX==1&&prevY==0) {
                                stepX=0;
                                stepY=-1;
                            }
                            else {
                                stepX=0;
                                stepY=1;
                            }
                            break;
                        default:
                            stepX = 0;
                            stepY = 0;
                            closedLoop = true;
                            Gdx.app.log("Triangulation","Issue. Found 0 or 15");
                            break;
                    }
                    // moving onto next point
                    pX+=stepX;
                    pY+=stepY;
                    //Contour
                    result.setColor(c);
                    result.drawPixel(pX,pY);

                    // saving contour point
                    contourVector.add(pX);
                    contourVector.add(pY);
                    count++;
                    prevX=stepX;
                    prevY=stepY;

                    // if we returned to the first point visited, the loop has finished
                    if (pX==startPoint.x&&pY==startPoint.y) {
                        closedLoop=true;
                        Gdx.app.log("Triangulation","Done with "+Integer.toString(count)+" points");
                    }
                }
            }
            return contourVector;
        }

        private Vector2 getStartingPixel(Pixmap pixmap){
            // finding the starting pixel is a matter of brute force, we need to scan
            // the image pixel by pixel until we find a non-transparent pixel
            Vector2 zeroPoint = new Vector2(0.f,0.f);

            for (int i = 1; i < pixmap.getWidth(); i++) {
                for (int j = 1; j < pixmap.getHeight(); j++) {
                    zeroPoint.x = (float)(i);
                    zeroPoint.y = (float)(j);

                    Color temp = new Color();
                    temp.rgba8888ToColor(temp,pixmap.getPixel(i-1,j-1));
                    float alpha11 = temp.a;
                    temp.rgba8888ToColor(temp,pixmap.getPixel(i,i-1));
                    float alpha12 = temp.a;
                    temp.rgba8888ToColor(temp,pixmap.getPixel(i-1,j));
                    float alpha21 = temp.a;
                    temp.rgba8888ToColor(temp,pixmap.getPixel(i,j));
                    float alpha22 = temp.a;

                    int val = getSquareValue(alpha11,alpha12,alpha21,alpha22);
                    if (val > 0 && val < 15) {
                        Gdx.app.log("Triangulation","Found starting pixel : ("+Float.toString(zeroPoint.x)+","+Float.toString(zeroPoint.y)+")");
                        return zeroPoint;
                    }
                }
            }
            return null;
        }

        private int getSquareValue(float alpha11,float alpha12,float alpha21,float alpha22){
			/*

			checking the 2x2 pixel grid, assigning these values to each pixel, if not transparent

			+---+---+
			| 1 | 2 | <- alpha12
			+---+---+
			| 4 | 8 | <- alpha22, pixel
			+---+---+

			*/
            int squareValue = 0;
            // checking upper left pixel
            if (alpha11 >= tolerance) {
                squareValue+=1;
            }
            // checking upper pixel
            if (alpha12 > tolerance) {
                squareValue+=2;
            }
            // checking left pixel
            if (alpha21 > tolerance) {
                squareValue+=4;
            }
            // checking the pixel itself
            if (alpha22 > tolerance) {
                squareValue+=8;
            }
            return squareValue;
        }
/*
    public FloatArray RDP(FloatArray v, float epsilon){
        Vector2 firstPoint = new Vector2();
        firstPoint.x = v.get(0);
        firstPoint.y = v.get(1);

        Vector2 lastPoint = new Vector2();
        lastPoint.x = v.get(v.size-2);
        lastPoint.y = v.get(v.size-1);//A partir de 0 ou 1

        if (v.size<3) {
            return v;
        }

        int index=-1;
        float dist=0.f;
        for (int i=1; i<v.size-1; i++) {
            float cDist = findPerpendicularDistance(v.get(i),firstPoint,lastPoint);
            if (cDist>dist) {
                dist=cDist;
                index=i;
            }
        }
        if (dist>epsilon) {
            FloatArray l1=v.slice(0,index+1);
            FloatArray l2=v.slice(index);
            FloatArray r1=RDP(l1,epsilon);
            FloatArray r2=RDP(l2,epsilon);
            FloatArray rs=r1.slice(0,r1.size-1).concat(r2);
            return rs;
        }
        else {
            FloatArray f = new FloatArray();
            f.add(firstPoint.x);
            f.add(firstPoint.y);
            f.add(lastPoint.x);
            f.add(lastPoint.y);
            return f;
        }
        return null;
    }

    private function findPerpendicularDistance(p:Point, p1:Point,p2:Point) {
        var result;
        var slope;
        var intercept;
        if (p1.x==p2.x) {
            result=Math.abs(p.x-p1.x);
        }
        else {
            slope = (p2.y - p1.y) / (p2.x - p1.x);
            intercept=p1.y-(slope*p1.x);
            result = Math.abs(slope * p.x - p.y + intercept) / Math.sqrt(Math.pow(slope, 2) + 1);
        }
        return result;
    }*/
}
