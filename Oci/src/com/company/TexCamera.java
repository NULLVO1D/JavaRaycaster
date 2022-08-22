package com.company;
/*
import java.awt.*;
import java.util.Vector;

public class TexCamera extends Camera{
    final int texWidth = 128;
    final int texHeight = 128;
    int[][] buffer = new int[Mappy.DEFAULT_HEIGHT][Mappy.DEFAULT_WIDTH];
    Vector texture[] = new Vector[8];
    public TexCamera(){
        for(int i = 0; i < 8; i++){
            texture[i] = new Vector<Integer>();
            texture[i].setSize(texWidth * texHeight);
        }
        //generate some textures
        for(int x = 0; x < texWidth; x++) {
            for (int y = 0; y < texHeight; y++) {
                int xorcolor = (x * 256 / texWidth) ^ (y * 256 / texHeight);
                //int xcolor = x * 256 / texWidth;
                int ycolor = y * 256 / texHeight;
                int xycolor = y * 128 / texHeight + x * 128 / texWidth;
                texture[0].setElementAt(65536 * 254 * ((x != y && x != texWidth - y) ? 1 : 0), texWidth * y + x); //flat red texture with black cross
                texture[1].setElementAt( xycolor + 256 * xycolor + 65536 * xycolor,texWidth * y + x); //sloped greyscale
                texture[2].setElementAt(256 * xycolor + 65536 * xycolor,texWidth * y + x) ; //sloped yellow gradient
                texture[3].setElementAt(xorcolor + 256 * xorcolor + 65536 * xorcolor,texWidth * y + x); //xor greyscale
                texture[4].setElementAt(256 * xorcolor,texWidth * y + x); //xor green
                texture[5].setElementAt(65536 * 192 * (x % 16 + y % 16),texWidth * y + x); //red bricks
                texture[6].setElementAt(65536 * ycolor,texWidth * y + x); //red gradient
                texture[7].setElementAt(128 + 256 * 128 + 65536 * 128,texWidth * y + x); //flat grey texture
            }
        }
    }

    @Override
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



            //texturing calculations
            int texNum = mapSelection[mapX][mapY] - 1; //1 subtracted from it so that texture 0 can be used!

            //calculate value of wallX
            double wallX; //where exactly the wall was hit
            if (side == 0) wallX = posY + perpendicularWallDist * rayDirY;
            else           wallX = posX + perpendicularWallDist * rayDirX;
            wallX -= Math.floor((wallX));

            //x coordinate on the texture
            int texX = (int)(wallX * (double)(texWidth));
            if(side == 0 && rayDirX > 0) texX = texWidth - texX - 1;
            if(side == 1 && rayDirY < 0) texX = texWidth - texX - 1;

            // How much to increase the texture coordinate per screen pixel
            double step = 1.0 * texHeight / lineHeight;
            // Starting texture coordinate
            double texPos = (drawStart - Mappy.DEFAULT_HEIGHT / 2 + lineHeight / 2) * step;
            for(int y = drawStart; y<drawEnd; y++)
            {
                // Cast the texture coordinate to integer, and mask with (texHeight - 1) in case of overflow
                int texY = (int)texPos & (texHeight - 1);
                texPos += step;
                Color32 color = (Color) texture[texNum].get(texHeight * texY + texX);
                //make color darker for y-sides: R, G and B byte each divided through two with a "shift" and an "and"
                if(side == 1) color = color.darker().darker();
                buffer[y][x] = (int) color;
            }
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
}
*/