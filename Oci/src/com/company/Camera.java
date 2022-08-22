package com.company;

import java.awt.*;
import java.util.Collection;

public class Camera{

    protected final int FPS_TARGET = 60;
    protected double posX = 22, posY = 12;  //x and y start position
    protected double dirX = -1, dirY = 0; //initial direction vector
    protected double planeX = 0, planeY = 0.66; //the "camera plane"
    protected double time = 0; //time of current frame
    protected double oldTime = 0; //time of previous frame
    protected byte[][] mapSelection = Mappy.debugMap2; //Map choice
    protected Screen targetScreen; //Screen to render to

    public void drawFrameOn(Screen screen) {

        targetScreen = screen;

        for (int x = 0; x < Mappy.DEFAULT_WIDTH; x++) {

            //calculate ray position and direction
            double cameraX = 2 * x / (double) Mappy.DEFAULT_WIDTH - 1; //x-coordinate in camera space
            double rayDirX = dirX + planeX * cameraX;
            double rayDirY = dirY + planeY * cameraX;
            //which box of the map we're in
            int mapX = (int) posX;
            int mapY = (int) posY;

            //length of ray from current position to next x or y-side
            double sideDistX;
            double sideDistY;

            //length of ray from one x or y-side to next x or y-side
            //these are derived as:
            //deltaDistX = sqrt(1 + (rayDirY * rayDirY) / (rayDirX * rayDirX))
            //deltaDistY = sqrt(1 + (rayDirX * rayDirX) / (rayDirY * rayDirY))
            //which can be simplified to abs(|rayDir| / rayDirX) and abs(|rayDir| / rayDirY)
            //where |rayDir| is the length of the vector (rayDirX, rayDirY). Its length,
            //unlike (dirX, dirY) is not 1, however this does not matter, only the
            //ratio between deltaDistX and deltaDistY matters, due to the way the DDA
            //stepping further below works. So the values can be computed as below.
            // Division through zero is prevented, even though technically that's not
            // needed in C++ with IEEE 754 floating point values.
            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);

            double perpendicularWallDist;

            //what direction to step in x or y-direction (either +1 or -1)
            int stepX;
            int stepY;

            int hit = 0; //was there a wall hit?
            int side = 0; //was a NS or an EW wall hit?
            //calculate step and initial sideDist
            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (posX - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - posX) * deltaDistX;
            }
            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (posY - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - posY) * deltaDistY;
            }
            //perform DDA
            while (hit == 0) {
                //jump to next map square, either in x-direction, or in y-direction
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                //Check if ray has hit a wall
                if (mapSelection[mapX][mapY] > 0) hit = 1;
            }
            //Calculate distance projected on camera direction. This is the shortest distance from the point where the wall is
            //hit to the camera plane. Euclidean to center camera point would give fisheye effect!
            //This can be computed as (mapX - posX + (1 - stepX) / 2) / rayDirX for side == 0, or same formula with Y
            //for size == 1, but can be simplified to the code below thanks to how sideDist and deltaDist are computed:
            //because they were left scaled to |rayDir|. sideDist is the entire length of the ray above after the multiple
            //steps, but we subtract deltaDist once because one step more into the wall was taken above.
            if (side == 0) perpendicularWallDist = (sideDistX - deltaDistX);
            else perpendicularWallDist = (sideDistY - deltaDistY);

            //Calculate height of line to draw on screen
            int offset = 25;
            int lineHeight = (int) (Mappy.DEFAULT_HEIGHT / perpendicularWallDist + offset);

            //calculate lowest and highest pixel to fill in current stripe
            int drawStart = -lineHeight / 2 + Mappy.DEFAULT_HEIGHT / 2;
            if (drawStart < 0) drawStart = 0;
            int drawEnd = lineHeight / 2 + Mappy.DEFAULT_HEIGHT / 2;
            if (drawEnd >= Mappy.DEFAULT_HEIGHT) drawEnd = Mappy.DEFAULT_HEIGHT - 1;

            /*Render*/

            //choose wall color
            Color color = switch (mapSelection[mapX][mapY]) {
                case 1 -> Color.RED; //red
                case 2 -> Color.GREEN; //green
                case 3 -> Color.BLUE; //blue
                case 4 -> Color.WHITE; //white
                default -> Color.YELLOW; //yellow
            };

            //give x and y sides different brightness
            if (side == 1) {
                color = color.darker().darker();
            }

            //draw the pixels of the stripe as a vertical line
            verLine(x, drawStart, drawEnd, color);


        }

        //timing for input and FPS counter
        oldTime = time;
        time = getTicks();
        double frameTime = (time - oldTime) / 1000000000.0; //frameTime is the time this frame has taken, in seconds

        double targetTime = (1.0 / FPS_TARGET);

        if (targetTime > frameTime) {
            long waitTimeMilli = (long) (targetTime * 1000 - frameTime * 1000);
            try {
                Thread.sleep(waitTimeMilli);
            } catch (InterruptedException e) {
                return;
            }
        }

        time = getTicks();
        frameTime = (time - oldTime) / 1000000000.0;

        screen.setTitle("OCI(" + (int) (1.0 / frameTime) + " FPS)"); //FPS counter
        targetScreen.repaint();
        targetScreen.innerContent.resetPixels();

        //speed modifiers
        double moveSpeed = frameTime * 5.0; //the constant value is in squares/second
        double rotSpeed = frameTime * 3.0; //the constant value is in radians/second

        Action input_vertical = screen.actionVertical;
        Action input_horizontal = screen.actionHorizontol;
        //move forward if no wall in front of you
        if (input_vertical == Action.FORWARD) {
            if (mapSelection[(int) (posX + dirX * moveSpeed)][(int) (posY)] == 0) posX += dirX * moveSpeed;
            if (mapSelection[(int) (posX)][(int) (posY + dirY * moveSpeed)] == 0) posY += dirY * moveSpeed;
        }
        //move backwards if no wall behind you
        if (input_vertical == Action.BACKWARD) {
            if (mapSelection[(int) (posX - dirX * moveSpeed)][(int) (posY)] == 0) posX -= dirX * moveSpeed;
            if (mapSelection[(int) (posX)][(int) (posY - dirY * moveSpeed)] == 0) posY -= dirY * moveSpeed;
        }
        //rotate to the right
        if (input_horizontal == Action.TURN_RIGHT) {
            //both camera direction and camera plane must be rotated
            double oldDirX = dirX;
            dirX = dirX * Math.cos(-rotSpeed) - dirY * Math.sin(-rotSpeed);
            dirY = oldDirX * Math.sin(-rotSpeed) + dirY * Math.cos(-rotSpeed);
            double oldPlaneX = planeX;
            planeX = planeX * Math.cos(-rotSpeed) - planeY * Math.sin(-rotSpeed);
            planeY = oldPlaneX * Math.sin(-rotSpeed) + planeY * Math.cos(-rotSpeed);
        }
        //rotate to the left
        if (input_horizontal == Action.TURN_LEFT) {
            //both camera direction and camera plane must be rotated
            double oldDirX = dirX;
            dirX = dirX * Math.cos(rotSpeed) - dirY * Math.sin(rotSpeed);
            dirY = oldDirX * Math.sin(rotSpeed) + dirY * Math.cos(rotSpeed);
            double oldPlaneX = planeX;
            planeX = planeX * Math.cos(rotSpeed) - planeY * Math.sin(rotSpeed);
            planeY = oldPlaneX * Math.sin(rotSpeed) + planeY * Math.cos(rotSpeed);
        }
    }

    protected double getTicks() {
        return System.nanoTime();
    }

    protected void verLine(int x, int drawStart, int drawEnd, Color color) {
        targetScreen.verLines[x].y = drawStart;
        targetScreen.verLines[x].height = (drawEnd - drawStart);
        targetScreen.verLines[x].color = color;
    }

}
