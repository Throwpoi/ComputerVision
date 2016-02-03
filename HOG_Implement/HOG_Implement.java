import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;


class cell_histrogram{
	double[] bin=new double[9];
}

public class HOG_Implement {
	public static void Magnitude_Angle_Calculated(int[][] matrix, int height, int width, 
			int[][] magnitude, double[][] angle){ //calculate magnitude and angle
		int i,j;
		for(i=0;i<height;i++){ //for the most upper and bottom row magnitude=0.angle=-1(out of bound)
			magnitude[i][0]=magnitude[i][width-1]=0;
			angle[i][0]=angle[i][width-1]=Double.MAX_VALUE; //if undefined, give it Double.max_value
		}
		for(i=0;i<width;i++){ //for the most left and right column magnitude=0.angle=-1(out of bound)
			magnitude[0][i]=magnitude[height-1][i]=0;
			angle[0][i]=angle[height-1][i]=Double.MAX_VALUE;
		}
		for(i=1;i<height-1;i++){
			for(j=1;j<width-1;j++){
				int x=matrix[i][j+1]-matrix[i][j-1];     //use template to calculate f/x
				int y=matrix[i+1][j]-matrix[i-1][j];     //use template to calculate f/y
				magnitude[i][j]=(int) Math.round(Math.sqrt((x*x+y*y)/2)); //calculate magnitude
				if(y==0&&x==0){      //if f/x=0=f/y, undefined area, magnitude=0, angle=Double.MAX_VALUE
					angle[i][j]=Double.MAX_VALUE;
					magnitude[i][j]=0;
					continue;
				}
				double temp=0-((x==0)?(Math.PI/2):Math.atan(y/x));  //calculate rad
				temp=temp*180/Math.PI;      //change rad to angle
				angle[i][j]=temp;
			}
		}
	}
	
	public static double[] compute_HOG_Feature(int[][] magnitude, double[][] angle){
		double[] HOG=new double[3780];                      //initiate an HOG descriptor
		cell_histrogram[][] cell=new cell_histrogram[16][8];       //This is used to store the cell, total num is 128
		//double[] block=new double[36];
		for(int i=0;i<16;i++){
			for(int j=0;j<8;j++){
				cell[i][j]=new cell_histrogram();                  //initiate every cell
				for(int k=i*8;k<i*8+8;k++){
					for(int t=j*8;t<j*8+8;t++){
						if(magnitude[k][t]==0) continue;           //Undefined, no need to calculate it.
						double temp=(angle[k][t]<0)?(angle[k][t]+180):angle[k][t];   //Make the negative to positive, cause we use 9 bins
						if(temp>=0&&temp<10)                                              
							cell[i][j].bin[0]+=magnitude[k][t]*(1-(10-temp)/20);     //0-10   vote
						else if(temp>=10&&temp<30){
							cell[i][j].bin[0]+=magnitude[k][t]*(1-(temp-10)/20);
						    cell[i][j].bin[1]+=magnitude[k][t]*(1-(30-temp)/20);     //10-30  vote
						}
						else if(temp>=30&&temp<50){
							cell[i][j].bin[1]+=magnitude[k][t]*(1-(temp-30)/20);
						    cell[i][j].bin[2]+=magnitude[k][t]*(1-(50-temp)/20);     //30-50  vote
						}
						else if(temp>=50&&temp<70){
							cell[i][j].bin[2]+=magnitude[k][t]*(1-(temp-50)/20);
						    cell[i][j].bin[3]+=magnitude[k][t]*(1-(70-temp)/20);     //50-70  vote
						}
						else if(temp>=70&&temp<90){
							cell[i][j].bin[3]+=magnitude[k][t]*(1-(temp-70)/20);
						    cell[i][j].bin[4]+=magnitude[k][t]*(1-(90-temp)/20);     //70-90  vote
						}
						else if(temp>=90&&temp<110){
							cell[i][j].bin[4]+=magnitude[k][t]*(1-(temp-90)/20);
						    cell[i][j].bin[5]+=magnitude[k][t]*(1-(110-temp)/20);    //90-110  vote
						}
						else if(temp>=110&&temp<130){
							cell[i][j].bin[5]+=magnitude[k][t]*(1-(temp-110)/20);
						    cell[i][j].bin[6]+=magnitude[k][t]*(1-(130-temp)/20);    //110-130  vote
						}
						else if(temp>=130&&temp<150){
							cell[i][j].bin[6]+=magnitude[k][t]*(1-(temp-130)/20);
						    cell[i][j].bin[7]+=magnitude[k][t]*(1-(150-temp)/20);    //130-150  vote
						}
						else if(temp>=150&&temp<170){
							cell[i][j].bin[7]+=magnitude[k][t]*(1-(temp-150)/20);
						    cell[i][j].bin[8]+=magnitude[k][t]*(1-(170-temp)/20);    //150-170  vote
						}
						else
							cell[i][j].bin[8]+=magnitude[k][t]*(1-(temp-170)/20);    //170-180  vote
					}
				}
			}
		}
		int h=0;
		for(int i=0;i<15;i++){
			for(int j=0;j<7;j++){
				cell_histrogram temp1=cell[i][j];           //Four cells for one block
				cell_histrogram temp2=cell[i+1][j];
				cell_histrogram temp3=cell[i][j+1];
				cell_histrogram temp4=cell[i+1][j+1];
				double total=0;                             //Initialize the N-2 norm
				for(int k=h;k<h+9;k++){HOG[k]=temp1.bin[k-h]; total+=HOG[k]*HOG[k];}      //Put histogram to HOG descriptor
				for(int k=h+9;k<h+18;k++){HOG[k]=temp2.bin[k-h-9]; total+=HOG[k]*HOG[k];}
				for(int k=h+18;k<h+27;k++){HOG[k]=temp3.bin[k-h-18]; total+=HOG[k]*HOG[k];}
				for(int k=h+27;k<h+36;k++){HOG[k]=temp4.bin[k-h-27]; total+=HOG[k]*HOG[k];}
				total=Math.sqrt(total); 
				for(int k=h;k<h+36;k++)
					if(total!=0)
						HOG[k]/=total;                //Normalize all 3780 number
				h+=36;
			}
		}
		return HOG;
	}
	
	public static double[] compute_HOG(String fileName){
		File file=new File(fileName);                   //open the image
		BufferedImage image=null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   //read the image
		//java.awt.image.Raster a=image.getData();  //get the data(pixels)
		int[][] magnitude=new int[128][64];       //initiate magnitude array
		double[][] angle=new double[128][64];           //initiate angle array
		int[][] matrix=new int[128][64];          //initiate matrix array.
		for(int i=16;i<144;i++){
			for(int j=16;j<80;j++){
				int pixel=image.getRGB(j, i);
				int R=(pixel&0xff0000)>>16,G=(pixel&0xff00)>>8,B=(pixel&0xff);   
				matrix[i-16][j-16]=(int)(0.21*R+0.72*G+0.07*B); //put gray value into matrix array
			}
		}	
		Magnitude_Angle_Calculated(matrix, 128, 64, magnitude, angle);   //Calculate magnitude and angle
		double[] HOG=compute_HOG_Feature(magnitude, angle);      //Calculate HOG
		return HOG;
	}
	
	public static double[] compute_Euclidean (String[] fileName, String resultName) throws IOException{
		double[][] temp=new double[10][3780];                //10 different descriptor
		double[] mean=new double[3780];                      //mean descriptor
		double[] EuclideanDistance=new double[10];           //10 Euclidean distance
		for(int i=0;i<10;i++)
			temp[i]=compute_HOG(fileName[i]);                //compute HOG descriptor for every image
		for(int i=0;i<3780;i++){
			for(int j=0;j<10;j++){
				mean[i]+=temp[j][i];             
			}
			mean[i]/=10;                                     //calculate mean descriptor
		}
		FileWriter fileWriter=new FileWriter(resultName);
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(mean[36*i+j]);   //Round to only 2 digits!
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
		for(int i=0;i<10;i++){
			double total=0;
			for(int j=0;j<3780;j++){
				total+=(temp[i][j]-mean[j])*(temp[i][j]-mean[j]);       //Calculate Euclidean Distance
			}
			EuclideanDistance[i]=Math.sqrt(total);
		}
		return EuclideanDistance;
	}
	
	public static double[] train(String[] positiveName, String[] negativeName) throws IOException{
		double[][] temp1=new double[10][3780];                    //temp1 for 10 positive images
		double[][] temp2=new double[10][3780];                    //temp1 for 10 negative images
		double[] W=new double[3781];                              //initialize W
		int iteration=0;
		for(int i=0;i<10;i++)
			temp1[i]=compute_HOG(positiveName[i]);               //Compute HOG descriptors for positive images
		for(int i=0;i<10;i++)
			temp2[i]=compute_HOG(negativeName[i]);               //Compute HOG descriptors for negative images
		while(true){
			boolean flag=true;
			for(int j=0;j<10;j++){
				double k=0;
				for(int i=0;i<3780;i++)	k+=W[i]*temp1[j][i];		//Calculate result	
				k+=W[3780];
				if(k<=0){
					for(int i=0;i<3780;i++) W[i]+=0.01*temp1[j][i]; //If not <=0,add 0.01 times descriptor and make flag false
					W[3780]+=0.01;flag=false;
				}	
			}
			for(int j=0;j<10;j++){
				double k=0;
				for(int i=0;i<3780;i++)	k+=W[i]*temp2[j][i];			
				k+=W[3780];
				if(k>=0){
					for(int i=0;i<3780;i++) W[i]-=0.01*temp2[j][i]; //If not >=0,add 0.01 times descriptor and make flag false
					W[3780]-=0.01;flag=false;
				}	
			}
			iteration++;      //Iteration number plus 1
			if(flag) break;   //If all the calculation are correct, break the iteration
		}
		FileWriter fileWriter=new FileWriter("Train_And_Test.txt");
		fileWriter.write("The initial W is all 0.\r\nThe learning rate is 0.01.\r\nThe order of training samples is 10 pos and 10 neg.\r\n");
		fileWriter.write("The number of iteration is "+iteration);
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
		return W;
	}
	
	public static int test(double[] W, double[] HOG){
		double total=0;
		for(int i=0;i<3780;i++)
			total+=W[i]*HOG[i];
		total+=W[3780];
		if(total>0) return 1;      //If >0, return 1, means human.
		else return -1;            //If <0, return -1, means not human.
	}
	
	public static void main(String[] args) throws IOException{
		FileWriter fileWriter=new FileWriter("crop001030c.txt");
		double[] HOG=compute_HOG("./positive/crop001030c.bmp");            //Compute all the required descriptor
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(HOG[36*i+j]);
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.flush();fileWriter.close();
		fileWriter=new FileWriter("crop001034b.txt");
		HOG=compute_HOG("./positive/crop001034b.bmp");
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(HOG[36*i+j]);
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
		fileWriter=new FileWriter("00000003a_cut.txt");
		HOG=compute_HOG("./negative/00000003a_cut.bmp");
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(HOG[36*i+j]);
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();	
		fileWriter=new FileWriter("00000057a_cut.txt");
		HOG=compute_HOG("./negative/00000057a_cut.bmp");
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(HOG[36*i+j]);
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();	
		fileWriter=new FileWriter("crop001008b.txt");
		HOG=compute_HOG("./test_positive/crop001008b.bmp");
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(HOG[36*i+j]);
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
		fileWriter=new FileWriter("00000053a_cut.txt");
		HOG=compute_HOG("./test_negative/00000053a_cut.bmp");
		for(int i=0;i<105;i++){
			for(int j=0;j<36;j++){
				DecimalFormat df = new DecimalFormat("0.0");String num = df.format(HOG[36*i+j]);
				fileWriter.write(num+" ");
			}
			fileWriter.write("\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
		String[] trainPositiveName={"./positive/crop001030c.bmp","./positive/crop001034b.bmp",
				"./positive/crop001063b.bmp","./positive/crop001070a.bmp","./positive/crop001275b.bmp",
				"./positive/crop001278a.bmp","./positive/crop001500b.bmp","./positive/crop001672b.bmp",
				"./positive/person_and_bike_026a.bmp","./positive/person_and_bike_151a.bmp"};
		String[] trainNegativeName={"./negative/01-03e_cut.bmp","./negative/00000003a_cut.bmp",
				"./negative/00000057a_cut.bmp","./negative/00000090a_cut.bmp","./negative/00000091a_cut.bmp",
				"./negative/00000118a_cut.bmp","./negative/no_person__no_bike_219_cut.bmp",
				"./negative/no_person__no_bike_258_Cut.bmp","./negative/no_person__no_bike_259_cut.bmp",
				"./negative/no_person__no_bike_264_cut.bmp"};
		String[] testPositiveName={"./test_positive/crop_000010b.bmp","./test_positive/crop001008b.bmp",
				"./test_positive/crop001028a.bmp","./test_positive/crop001045b.bmp","./test_positive/crop001047b.bmp"};
		String[] testNegativeName={"./test_negative/00000053a_cut.bmp","./test_negative/00000062a_cut.bmp","./test_negative/00000093a_cut.bmp",
				"./test_negative/no_person__no_bike_213_cut.bmp","./test_negative/no_person__no_bike_247_cut.bmp"};
		double[] positive=compute_Euclidean(trainPositiveName,"positiveMean.txt");  //Computer Euclidean Distance
		double[] negative=compute_Euclidean(trainNegativeName,"negativeMean.txt");
		fileWriter=new FileWriter("Euclidean_positive.txt");
		for(int i=0;i<10;i++)
			fileWriter.write(trainPositiveName[i]+": "+positive[i]+"\r\n");
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
		fileWriter=new FileWriter("Euclidean_negative.txt");
		for(int i=0;i<10;i++)
			fileWriter.write(trainNegativeName[i]+": "+negative[i]+"\r\n");
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();		
		double[] W=train(trainPositiveName,trainNegativeName);   //Train the positive and negative images
		fileWriter=new FileWriter("Test_Result.txt");
		for(int i=0;i<5;i++){
			double[] HOG1=compute_HOG(testPositiveName[i]);
			fileWriter.write("Test result of "+testPositiveName[i]+" is "); //Test the positive images, if return 1 human, -1 not human
			if(test(W,HOG1)==1) fileWriter.write("human.\r\n");
			else fileWriter.write("not human.\r\n");
		}
		for(int i=0;i<5;i++){
			double[] HOG2=compute_HOG(testNegativeName[i]);
			fileWriter.write("Test result of "+testNegativeName[i]+" is "); //Test the negative images, if return 1 human, -1 not human
			if(test(W,HOG2)==1) fileWriter.write("human.\r\n");
			else fileWriter.write("not human.\r\n");
		}
		fileWriter.write("\r\n");fileWriter.flush();fileWriter.close();
	}
}
