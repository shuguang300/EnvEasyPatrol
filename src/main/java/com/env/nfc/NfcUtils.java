package com.env.nfc;

import java.io.IOException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;

public class NfcUtils {
	private NfcUtils() {

	}

	public static final int tagCardType = 0;
	public static final int userCardType = 1;
	public static final String[] intentActions = new String[] { 
		NfcAdapter.ACTION_NDEF_DISCOVERED, 
		NfcAdapter.ACTION_TAG_DISCOVERED, 
		NfcAdapter.ACTION_TECH_DISCOVERED 
	};

	public static final String[][] techList = new String[][] {};

	public static final IntentFilter[] intentFilters = new IntentFilter[] { 
		new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED), 
		new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
		new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) 
	};

	public static NdefMessage StringToNDEF(String arg0) {
		byte[] textBytes = arg0.getBytes();
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[] {}, textBytes);
		return new NdefMessage(new NdefRecord[] { textRecord });
	}

	public static PendingIntent NFCPendingIntent(Context context) {
		// if(context.getClass().getName().equals(SplashActivity.class.getName())){
		// Intent intent = new Intent(Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent.setClass(context, SplashActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		// PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		// intent, 0);
		// return pendingIntent;
		// }else {
		// return PendingIntent.getActivity(context, 0, new Intent(context,
		// context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// }
		return PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	public static NdefMessage[] processNDEF(Intent intent) {
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				String result = "";
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				for (int i = 0; i < msgs.length; i++) {
					for (int j = 0; j < msgs[i].getRecords().length; j++) {
						result = result + new String(msgs[i].getRecords()[j].getPayload());
					}
				}
			} else {
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		}
		return msgs;
	}

	public static boolean writeToNDEF(NdefMessage message, Intent intent) {
		boolean ok = false;
		for (String intentfilter : NfcUtils.intentActions) {
			if (intent.getAction().equals(intentfilter)) {
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				Ndef ndef = Ndef.get(tag);
				if (ndef != null) {
					try {
						ndef.connect();
						if (ndef.isWritable()) {
							if (message.toByteArray().length > ndef.getMaxSize()) {
								ok = false;
							} else {
								ndef.writeNdefMessage(message);
								ok = true;
							}
						} else {

						}
					} catch (IOException e) {
						e.printStackTrace();
						ok = false;
					} catch (FormatException e) {
						e.printStackTrace();
						ok = false;
					}
				} else {
					NdefFormatable format = NdefFormatable.get(tag);
					if (format != null) {
						try {
							format.connect();
							format.format(message);
							ok = true;
						} catch (IOException e) {
							e.printStackTrace();
							ok = false;
						} catch (FormatException e) {
							e.printStackTrace();
							ok = false;
						}
					}
				}
			}
		}
		return ok;
	}

	public static boolean writeToBlock(String arg0, Intent intent) {
		boolean ok = false;
		for (String intentfilter : NfcUtils.intentActions) {
			if (intent.getAction().equals(intentfilter)) {
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				MifareClassic mfc = MifareClassic.get(tag);
				try {
					mfc.connect();
					if (mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_NFC_FORUM)) {
						mfc.writeBlock(4, NfcUtils.StringToBytes(arg0));
						ok = true;
					} else {
						ok = false;
					}
					mfc.close();
				} catch (IOException e) {
					ok = false;
					e.printStackTrace();
				}
			}
		}
		return ok;
	}

	public static String processIntent(Intent intent) {
		String metaInfo = "";
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		boolean auth = false;
		MifareClassic mfc = MifareClassic.get(tagFromIntent);
		try {
			// Enable I/O operations to the tag from this TagTechnology object.
			mfc.connect();
			String tagID = NfcUtils.bytesToHexString(tagFromIntent.getId());
			int type = mfc.getType();// 获取TAG的类型
			int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
			String typeS = "";
			switch (type) {
			case MifareClassic.TYPE_CLASSIC:
				typeS = "TYPE_CLASSIC";
				break;
			case MifareClassic.TYPE_PLUS:
				typeS = "TYPE_PLUS";
				break;
			case MifareClassic.TYPE_PRO:
				typeS = "TYPE_PRO";
				break;
			case MifareClassic.TYPE_UNKNOWN:
				typeS = "TYPE_UNKNOWN";
				break;
			}
			metaInfo += "卡片ID：" + tagID + "\n卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";
			for (int j = 0; j < sectorCount; j++) {
				// Authenticate a sector with key A.
				auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_NFC_FORUM);
				int bCount;
				int bIndex;
				if (auth) {
					metaInfo += "Sector " + j + ":验证成功\n";
					// 读取扇区中的块
					bCount = mfc.getBlockCountInSector(j);
					bIndex = mfc.sectorToBlock(j);
					for (int i = 0; i < bCount; i++) {
						byte[] data = mfc.readBlock(bIndex);
						metaInfo += "Block " + bIndex + " : " + new String(data) + "\n";
						bIndex++;
					}
				} else {
					metaInfo += "Sector " + j + ":验证失败\n";
				}
			}
			mfc.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return metaInfo;
	}

	public static String getTagID(Intent intent) {
		String tagID = "";
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		tagID = NfcUtils.bytesToHexString(tagFromIntent.getId());
		return tagID;
	}


	public static byte[] StringToBytes(String arg0) {
		byte[] value = arg0.getBytes();
		byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];
		for (int i = 0; i < MifareClassic.BLOCK_SIZE; i++) {
			if (i < value.length)
				toWrite[i] = value[i];
			else
				toWrite[i] = 0;
		}
		return toWrite;
	}

	// 字符序列转换为16进制字符串
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	// 字符序列转换为10进制字符串
	public static String bytesToDecimalsString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		if (src.length <= 4) {
			int addr = src[0] & 0xFF;
			addr |= ((src[1] << 8) & 0xFF00);
			addr |= ((src[2] << 16) & 0xFF0000);
			addr |= ((src[3] << 24) & 0xFF000000);
			stringBuilder.append(addr);
		}
		return stringBuilder.toString();
	}
}
