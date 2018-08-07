package loopchain.icon.wallet.core.response;

import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class BlockData {
	
    @SerializedName("version")
	private String _version;
    @SerializedName("block_hash")
	private String _blockHash;
    @SerializedName("height")
	private int _height;
    @SerializedName("prev_block_hash")
	private String _prevBlockHash;
    @SerializedName("merkle_tree_root_hash")
	private String _merkleTreeRoot;
    @SerializedName("time_stamp")
	private String _timeStamp;
    @SerializedName("confirmed_transaction_list")
    private JsonArray _confirmedTxList;
    @SerializedName("peer_id")
	private String _peerId;
    @SerializedName("signature")
	private String _signature;

	public String getVersion() {
		return _version;
	}
	
	public String getBlockHash() {
		return _blockHash;
	}

	public int getHeight() {
		return _height;
	}

	public String getPrevBlockHash() {
		return _prevBlockHash;
	}

	public String getMerkleTreeRoot() {
		return _merkleTreeRoot;
	}

	public String getTimeStamp() {
		return _timeStamp;
	}

	public Vector<TransactionData> getConfirmedTxList() {
		if(_confirmedTxList == null)
			return new Vector<TransactionData>();
		
		int length = _confirmedTxList.size();
		Vector<TransactionData> v = new Vector<TransactionData>();
		Gson gson = new Gson();
		for(int i=0; i<length; i++){
			JsonObject obj = _confirmedTxList.get(i).getAsJsonObject();
			TransactionData txData = gson.fromJson(obj, TransactionData.class);
			v.addElement(txData);
		}
		return v;
	}

	public String getPeerId() {
		return _peerId;
	}

	public String getSignature() {
		return _signature;
	}

}
