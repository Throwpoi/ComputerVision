package localEdge;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;;

public class Local_Edge {
	public static void Magnitude_Angle_Calculated(int[][] matrix, int height, int width, 
			int[][] magnitude, int[][] angle){ //calculate magnitude and quantized angle
		int i,j;
		for(i=0;i<height;i++){ //for the most upper and bottom row magnitude=0.angle=-1(out of bound)
			magnitude[i][0]=magnitude[i][width-1]=0;
			angle[i][0]=angle[i][width-1]=-1;
		}
		for(i=0;i<width;i++){ //for the most left and right column magnitude=0.angle=-1(out of bound)
			magnitude[0][i]=magnitude[height-1][i]=0;
			angle[0][i]=angle[height-1][i]=-1;
		}
		for(i=1;i<height-1;i++){
			for(j=1;j<width-1;j++){
				int x=matrix[i][j+1]-matrix[i][j-1];     //use template to calculate f/x
				int y=matrix[i+1][j]-matrix[i-1][j];     //use template to calculate f/y
				magnitude[i][j]=(int) Math.round(Math.sqrt((x*x+y*y)/2)); //calculate magnitude
				if(y==0&&x==0){      //if f/x=0=f/y, undefined area, magnitude=0, angle=-1
					angle[i][j]=-1;
					magnitude[i][j]=0;
					continue;
				}
				double temp=0-((x==0)?(Math.PI/2):Math.atan(y/x));  //calculate rad
				temp=temp*180/Math.PI;      //change rad to angle
				if(temp<0) temp+=180;       //add 180 for(-180,0), result is the same
				if(temp>=0&&temp<20) angle[i][j]=1;
				else if(temp>=20&&temp<40) angle[i][j]=2;
				else if(temp>=40&&temp<60) angle[i][j]=3;
				else if(temp>=60&&temp<80) angle[i][j]=4;
				else if(temp>=80&&temp<100) angle[i][j]=5;   //1-9 possible condition
				else if(temp>=100&&temp<120) angle[i][j]=6;
				else if(temp>=120&&temp<140) angle[i][j]=7;
				else if(temp>=140&&temp<160) angle[i][j]=8;
				else angle[i][j]=9;
			}
		}
	}
	
	public static double[][] LEOH(int[][] matrix, int height, int width, int[][] magnitude, int[][] angle){
		//calculate LEOH for every cell
		int row=height/16;      //row*column num of cell, every cell is 16*16
		int column=width/16;
		double[][] leoh=new double[row*column][9];  //use array to replace histogram
		for(int i=0;i<row;i++){
			for(int j=0;j<column;j++){      //for every cell
				double sum=0;               //sum means the addtion of all magnitude
			    double num=0;               //valid pixel's number
				for(int k=16*i;k<16*i+16;k++){
					for(int t=16*j;t<16*j+16;t++){
						if(angle[k][t]!=-1){   //only calculate valid pixels
							num++;             //valid number add 
							sum+=magnitude[k][t];  //sum add
						}
					}
				}
				sum/=num;         //average=sum/=num, now sum means average
				for(int k=16*i;k<16*i+16;k++){
					for(int t=16*j;t<16*j+16;t++){
						if(angle[k][t]!=-1){   //only calculate valid pixels
							//System.out.println(magnitude[k][t]/sum+"");
							System.out.print(angle[k][t]+" ");
							leoh[i*16+j][angle[k][t]-1]+=(magnitude[k][t]/sum);							
						} //angle[k][t] is the location of theta, add magnitude/average to this location
					}
				System.out.println("");
				}
			}				
		}
		return leoh;  //return histogram described by array
	}
	
	public static void main(String[] args) throws IOException{
		File file=new File("Lena256.bmp");        //open the image
		BufferedImage image=ImageIO.read(file);   //read the image
		java.awt.image.Raster a=image.getData();  //get the data(pixels)
		int width=image.getWidth();               //set width
		int height=image.getHeight();             //set height
		int[][] magnitude=new int[height][width]; //initiate magnitude array
		int[][] angle=new int[height][width];     //initiate angle array
		int[][] matrix=new int[height][width];    //initiate matrix array.
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				matrix[i][j]=a.getSample(i, j, 0); //put gray value into matrix array
			}
		}		
		Local_Edge.Magnitude_Angle_Calculated(matrix, height, width, magnitude, angle);
		//calculate the magnitude and quantized angle, result stored in two arrays
		BufferedImage grayImage=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		//make a new image the same width and height as the old one
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){	  //This is the quantize magnitude image
				grayImage.getRaster().setSample(i, j, 0, magnitude[i][j]);
			}
		System.out.println("");
		}
		File newFile = new File("Lena256New.bmp");   //create a new file
		ImageIO.write(grayImage, "bmp", newFile);    //put the image into the file
		double[][] leoh=Local_Edge.LEOH(matrix, height, width, magnitude, angle);//calculate leoh array
		FileWriter fileWriter=new FileWriter("Lena256.txt");   //create a new txt file
		fileWriter.write("In this histogram, I add every M(r,c)/Ave using double, and round the result to int in the final sum!\r\n");
		fileWriter.write("If you round double to int before add all M(r,c)/Ave, result will be a little bit different.\r\n");
		for(int i=0;i<leoh.length;i++){
			for(int j=0;j<leoh[0].length;j++)   //put histogram into txt file
				fileWriter.write(String.valueOf((int)leoh[i][j])+" ");
		fileWriter.write("\r\n");
		}
		fileWriter.flush(); 
	    fileWriter.close();          //close the file, finish the project
	}
};
