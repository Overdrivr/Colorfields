package com.overdrivr.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bart on 17/05/2015.
 */
public class GravityField {
    Vector2 [][] forces_red;
    Vector2 [][] forces_green;
    Vector2 [][] forces_blue;

    float unitsize;
    Vector2 gridcenter;//Bottom left actually
    int cellamount;

    public boolean debugDrawGrid;
    public boolean debugDrawForces;

    // @center : center of the grid
    // @amount : amount of grid cells per side
    // @cellsize : size of the side of a cell
    public GravityField(Vector2 center, float edgesize, int amount){
        forces_red = new Vector2[amount][amount];
        forces_blue = new Vector2[amount][amount];
        forces_green = new Vector2[amount][amount];

        unitsize = edgesize;
        gridcenter = center;
        cellamount = amount;
        debugDrawForces = true;
        debugDrawGrid = true;
        clearField();
    }

    public void clearField(){
        for(int x = 0 ; x < cellamount ; x++)
            for(int y = 0 ; y < cellamount ; y++){
                forces_red[x][y] = new Vector2(0,0);
                forces_green[x][y] = new Vector2(0,0);
                forces_blue[x][y] = new Vector2(0,0);
            }
    }

    public void addSphericalAttractor(Vector2 center, Color color){
        //Compute contribution to field in each grid point
        for(int x = 0 ; x < cellamount ; x++)
            for(int y = 0 ; y < cellamount ; y++){
                // Grid point position
                Vector2 localpoint = new Vector2(x * unitsize + gridcenter.x, y * unitsize + gridcenter.y);
                // Vector from grid point to center
                localpoint.x = center.x - localpoint.x;
                localpoint.y = center.y - localpoint.y;
                // Compute length
                float r = localpoint.len();
                // Gravitationnal constant * mass1 * mass2
                float Gm1m2 = 0.01f;

                if(r < 0.00001f)
                    localpoint.setLength(0.f);
                else {
                    float length = Gm1m2 / (r);//Find best formula for here
                    if (length > 0.1f)
                        length = 0.1f;
                    localpoint.setLength(length);
                }

                forces_red[x][y].x += localpoint.x * color.r;
                forces_red[x][y].y += localpoint.y * color.r;

                forces_green[x][y].x += localpoint.x * color.g;
                forces_green[x][y].y += localpoint.y * color.g;

                forces_blue[x][y].x += localpoint.x * color.b;
                forces_blue[x][y].y += localpoint.y * color.b;
            }
    }

    public Vector2 getForce(float x, float y, Color color){
        int x0 = fastfloor((x - gridcenter.x) / unitsize);
        int y0 = fastfloor((y - gridcenter.y) / unitsize);

        // Outside the grid, no force is applied
        if (x0 < 0 || x0 > cellamount-2 ||
            y0 < 0 || y0 > cellamount-2) {
            return new Vector2(0,0);
        }

        Vector2 t = new Vector2();
        // Get the internal distance to each of the four cell points
        // This distance is between 0 and 1
        t.set(x - x0,y - y0);
        float d00 = t.len();
        t.set(x - x0,y - y0 + 1);
        float d01 = t.len();
        t.set(x - x0 + 1,y - y0);
        float d10 = t.len();
        t.set(x - x0 + 1,y - y0 + 1);
        float d11 = t.len();

        // Get red vector
        Vector2 force_r = new Vector2(0,0);
        if(color.r > 0.f){
            force_r.x = (forces_red[x0][y0].x     * d00 +
                         forces_red[x0+1][y0].x   * d10 +
                         forces_red[x0][y0+1].x   * d10 +
                         forces_red[x0+1][y0+1].x * d10)/(d00 + d10 + d01 + d11);
            force_r.y = (forces_red[x0][y0].y     * d00 +
                         forces_red[x0+1][y0].y   * d10 +
                         forces_red[x0][y0+1].y   * d10 +
                         forces_red[x0+1][y0+1].y * d10)/(d00 + d10 + d01 + d11);
        }

        // Green vector
        Vector2 force_g = new Vector2(0,0);
        if(color.g > 0.f){
            force_g.x = (forces_green[x0][y0].x     * d00 +
                         forces_green[x0+1][y0].x   * d10 +
                         forces_green[x0][y0+1].x   * d10 +
                         forces_green[x0+1][y0+1].x * d10)/(d00 + d10 + d01 + d11);
            force_g.y = (forces_green[x0][y0].y     * d00 +
                         forces_green[x0+1][y0].y   * d10 +
                         forces_green[x0][y0+1].y   * d10 +
                         forces_green[x0+1][y0+1].y * d10)/(d00 + d10 + d01 + d11);
        }


        // Green vector
        Vector2 force_b = new Vector2(0,0);
        if(color.b > 0.f){
            force_b.x = (forces_blue[x0][y0].x     * d00 +
                         forces_blue[x0+1][y0].x   * d10 +
                         forces_blue[x0][y0+1].x   * d10 +
                         forces_blue[x0+1][y0+1].x * d10)/(d00 + d10 + d01 + d11);
            force_b.y = (forces_blue[x0][y0].y     * d00 +
                         forces_blue[x0+1][y0].y   * d10 +
                         forces_blue[x0][y0+1].y   * d10 +
                         forces_blue[x0+1][y0+1].y * d10)/(d00 + d10 + d01 + d11);
        }

        // Total force
        Vector2 force = new Vector2();
        force.x = force_r.x * color.r + force_g.x * color.g + force_b.x * color.b;
        force.y = force_r.y * color.r + force_g.y * color.g + force_b.y * color.b;

        return force;
    }

    public void debug_draw(ShapeRenderer renderer, Matrix4 combined){

        //Display grid
        if(debugDrawGrid){
            renderer.setColor(1, 1, 0, 1);
            for(int i = 0 ; i < cellamount ; i++) {
                float x1 = i * unitsize + gridcenter.x;
                float x2 = i * unitsize + gridcenter.x;
                float y1 = 0 * unitsize + gridcenter.y;
                float y2 = (cellamount - 1) * unitsize + gridcenter.y;
                renderer.line(x1,y1,x2,y2);
                x1 = 0 * unitsize + gridcenter.x;
                x2 = (cellamount - 1) * unitsize + gridcenter.x;
                y1 = i * unitsize + gridcenter.y;
                y2 = i * unitsize + gridcenter.y;
                renderer.line(x1,y1,x2,y2);
            }
        }

        // Display forces
        if(debugDrawForces){
            float x1,x2,y1,y2;
            for(int x = 0 ; x < cellamount ; x++)
                for(int y = 0 ; y < cellamount ; y++) {
                    //Red
                    renderer.setColor(1, 0, 0, 1);
                    x1 = x * unitsize + gridcenter.x;
                    y1 = y * unitsize + gridcenter.y;
                    x2 = x1 + forces_red[x][y].x*10;
                    y2 = y1 + forces_red[x][y].y*10;
                    renderer.line(x1,y1,x2,y2);

                    //Green
                    renderer.setColor(0, 1, 0, 1);
                    x1 = x * unitsize + gridcenter.x;
                    y1 = y * unitsize + gridcenter.y;
                    x2 = x1 + forces_green[x][y].x*10;
                    y2 = y1 + forces_green[x][y].y*10;
                    renderer.line(x1,y1,x2,y2);

                    //Blue
                    renderer.setColor(0, 0, 1, 1);
                    x1 = x * unitsize + gridcenter.x;
                    y1 = y * unitsize + gridcenter.y;
                    x2 = x1 + forces_blue[x][y].x*10;
                    y2 = y1 + forces_blue[x][y].y*10;
                    renderer.line(x1,y1,x2,y2);
            }
        }
    }

    private int fastfloor(float n)
    {
        return (n >= 0) ? (int)(n) : (int)(n-1);
    }
}
