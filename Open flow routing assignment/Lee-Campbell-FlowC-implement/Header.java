import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Header 
{
	public static final int  INFO_1_INDEX = 0;
	public static final int  INFO_2_INDEX = 4;
	public static final int  INFO_3_INDEX = 8;
	public static final int  INFO_4_INDEX = 12;
	public static int deConstruct(byte[] header, int index)
	{
		ByteBuffer tmpBuf = ByteBuffer.wrap(header);
		int tmpInt = tmpBuf.getInt(index);
	
		return tmpInt;
	}
	
	public static byte[] construct(byte[] header, int  info1, int info2, int info3)
	{
		
		ByteBuffer tmpBuf = ByteBuffer.wrap(header);

		tmpBuf.putInt(INFO_1_INDEX, info1);
		tmpBuf.putInt(INFO_2_INDEX, info2);
		tmpBuf.putInt(INFO_3_INDEX, info3);
		header = tmpBuf.array();
		
		return header;
	}
	
	public static byte[] construct(byte[] header, int info1, int info2, int info3, int info4)
	{
		
		ByteBuffer tmpBuf = ByteBuffer.wrap(header);

		tmpBuf.putInt(INFO_1_INDEX, info1);
		tmpBuf.putInt(INFO_2_INDEX, info2);
		tmpBuf.putInt(INFO_3_INDEX, info3);
		tmpBuf.putInt(INFO_4_INDEX, info4);
		header = tmpBuf.array();
		
		return header;
	}
	
	public static byte[] get(DatagramPacket packet)
	{
		byte[] header = new byte[Constant.HEADER_LENGTH];
		byte[] packetArray = packet.getData();
		
		System.arraycopy(packetArray, 0, header, 0, header.length);
	
		return header;
	}
	
	public static int getInt(DatagramPacket packet, int index)
	{
		byte[] content = packet.getData();
		ByteBuffer tmpBuf = ByteBuffer.wrap(content);
		int value = tmpBuf.getInt(index);
	
		return value;
		
	}
	
	

}
