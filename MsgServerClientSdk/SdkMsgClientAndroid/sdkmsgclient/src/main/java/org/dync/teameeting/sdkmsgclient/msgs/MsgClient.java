package org.dync.teameeting.sdkmsgclient.msgs;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;

import org.dync.teameeting.sdkmsgclient.jni.JMClientApp;
import org.dync.teameeting.sdkmsgclient.jni.JMClientHelper;
import org.dync.teameeting.sdkmsgclient.jni.JMClientType;
import org.dync.teameeting.sdkmsgclient.jni.JMSCbData;
import org.dync.teameeting.sdkmsgclient.jni.NativeContextRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import pms.CommonMsg;
import pms.EntityMsgType;
import pms.EntityMsg.Entity;

/**
 * Created by hp on 7/10/16.
 */
public class MsgClient implements JMClientHelper{

    private boolean                     mIsFetched = false;
    private String                      mStrUserId = null;
    private String                      mStrToken = null;
    private String                      mStrNname = null;
    private String                      mStrUicon = null;
    private HashMap<String, Long>       mGroupSeqn = null;
    private ArrayList<GroupInfo>        mGroupInfo = null;

    private NativeContextRegistry       mNativeContext = null;
    private JMClientApp                 mMApp = null;
    private Context                     mContext = null;
    private MSClientDelegate            mClientDelegate = null;
    private MSGroupDelegate             mGroupDelegate = null;
    private MSTxtMessageDelegate        mTxtMsgDelegate = null;

    private MSSqlite3Manager            mSqlite3Manager = null;
    private ReentrantLock               mReentrantLock = null;

    private static MsgClient ourInstance = new MsgClient();

    public static MsgClient getInstance() {
        return ourInstance;
    }

    private MsgClient() {
    }

    public String getmStrUserId() {
        return mStrUserId;
    }

    public void setmStrUserId(String mStrUserId) {
        this.mStrUserId = mStrUserId;
        if (null!=mMApp) {
            mMApp.SetUserId(mStrUserId);
        } else {
            System.err.println("setmStrUserId mMApp is null");
        }
    }

    public String getmStrToken() {
        return mStrToken;
    }

    public void setmStrToken(String mStrToken) {
        this.mStrToken = mStrToken;
        if (null!=mMApp) {
            mMApp.SetToken(mStrToken);
        } else {
            System.err.println("setmStrToken mMApp is null");
        }
    }

    public String getmStrNname() {
        return mStrNname;
    }

    public void setmStrNname(String mStrNname) {
        this.mStrNname = mStrNname;
        if (null!=mMApp) {
            mMApp.SetNickName(mStrNname);
        } else {
            System.err.println("setmStrNname mMApp is null");
        }
    }

    public String getmStrUicon() {
        return mStrUicon;
    }

    public void setmStrUicon(String mStrUicon) {
        this.mStrUicon = mStrUicon;
        if (null!=mMApp) {
            mMApp.SetUIconUrl(mStrUicon);
        } else {
            System.err.println("setmStrUicon mMApp is null");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////function for invoke////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    public int MCInit(Context context, String strUid, String strToken, String strNname) {
        assert(null == mMApp);
        mContext = context;
        mNativeContext = new NativeContextRegistry();
        mNativeContext.register(mContext);
        mMApp = new JMClientApp(getInstance());
        assert (null != mMApp);
        mStrUserId = strUid;
        mStrToken = strToken;
        mStrNname = strNname;

        mGroupSeqn = new HashMap<String, Long>();
        mGroupInfo = new ArrayList<GroupInfo>();
        mReentrantLock = new ReentrantLock();
        mSqlite3Manager = new MSSqlite3Manager();
        mSqlite3Manager.InitManager(context);
        CheckUserOrInit(mStrUserId);
        GetLocalSeqnsFromDb();
        SyncSeqnFromDb2Core();
        return mMApp.Init(strUid, strToken, strNname, CommonMsg.EModuleType.TLIVE_VALUE);
    }

    public int MCUnin() {
        if (null != mSqlite3Manager) {
            mSqlite3Manager.UninManager();
        }
        if (null != mMApp) {
            return mMApp.Unin();
        } else {
            return -10;
        }
    }

    public void MCSetTxtMsgDelegate(MSTxtMessageDelegate txtMsgDelegate) {
        mTxtMsgDelegate = txtMsgDelegate;
    }

    public void MCSetGroupDelegate(MSGroupDelegate groupDelegate) {
        mGroupDelegate = groupDelegate;
    }

    public void MCSetClientDelegate(MSClientDelegate clientDelegate) {
        mClientDelegate = clientDelegate;
    }

    public int MCRegisterMsgCb() {
        if (null != mMApp) {
            return mMApp.RegisterMsgCb();
        } else {
            return -10;
        }
    }

    public int MCUnRegisterMsgCb() {
        if (null != mMApp) {
            return mMApp.UnRegisterMsgCb();
        } else {
            return -10;
        }
    }

    public int MCConnToServer(String strServer, int port) {
        if (null != mMApp) {
            mMApp.ConnToServer(strServer, port);
            return 0;
        } else {
            return -10;
        }

    }

    public int MCAddGroup(String strGroupId) {
        if (null != mMApp) {
            mMApp.AddGroup(strGroupId);
            return 0;
        } else {
            return -10;
        }

    }

    public int MCRmvGroup(String strGroupId) {
        if (null != mMApp) {
            mMApp.RmvGroup(strGroupId);
            return 0;
        } else {
            return -10;
        }

    }

    public int MCSyncMsg() {
        if (mIsFetched) {
            System.out.println("MCSyncMsg sync all seqn");
            this.SyncAllSeqns();
        } else {
            System.out.println("MCSyncMsg should be fetched before called");
            this.FetchAllSeqns();
        }
        return 0;
    }

    public int MCSync2Db() {
        this.PutLocalSeqnsToDb();
        return 0;
    }

    public String MCSendTxtMsg(String strGroupId, String strContent) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndCont(strGroupId, strContent, JMClientType.MC_MSGTYPE_TTXT);
        if (null != mMApp && null != jsonMsg) {
            outMsgId = mMApp.SndMsg(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TTXT_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FGROUP_VALUE);
        }
        return outMsgId;
    }

    public String MCSendTxtMsgTos(String strGroupId, String[] arrUsers, String strContent) {
        if (arrUsers.length==0) return null;
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndCont(strGroupId, strContent, JMClientType.MC_MSGTYPE_TTXT);
        if (null != mMApp && null != jsonMsg) {
            outMsgId = mMApp.SndMsgTo(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TTXT_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FMULTI_VALUE, arrUsers, arrUsers.length);
        }
        return outMsgId;
    }

    public String MCSendTxtMsgToUsr(String strUserId, String strContent) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithUidAndCont(strUserId, strContent, JMClientType.MC_MSGTYPE_TTXT);
        if (null != mMApp && null != jsonMsg) {
            String[] arrUser = {strUserId};
            outMsgId = mMApp.SndMsgTo("groupid", "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TTXT_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FSINGLE_VALUE, arrUser, 1);
        }
        return outMsgId;
    }

    public String MCSendTxtMsgToUsrs(String[] arrUsers, String strContent) {
        if (arrUsers.length==0) return null;
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithUidsAndCont(arrUsers, strContent, JMClientType.MC_MSGTYPE_TTXT);
        if (null != mMApp && null != jsonMsg) {
            outMsgId = mMApp.SndMsgTo("groupid", "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TTXT_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FMULTI_VALUE, arrUsers, arrUsers.length);
        }
        return outMsgId;
    }

    public String MCNotifyLive(String strGroupId, String strHostId, int flag) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndUid(strGroupId, strHostId, flag, JMClientType.MC_MSGTYPE_TLIV);
        if (null != mMApp && null != jsonMsg) {
            outMsgId = mMApp.SndMsg(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TLIV_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FGROUP_VALUE);
        }
        return outMsgId;
    }

    public String MCNotifyRedEnvelope(String strGroupId, String strHostId, String strCash, String strCont) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndUid(strGroupId, strHostId, strCash, strCont, JMClientType.MC_MSGTYPE_TREN);
        if (null != mMApp && null != jsonMsg) {
            outMsgId =  mMApp.SndMsg(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TREN_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FGROUP_VALUE);
        }
        return outMsgId;
    }

    public String MCNotifyBlacklist(String strGroupId, String strUserId, int flag, String[] notifys) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndUid(strGroupId, strUserId, flag, JMClientType.MC_MSGTYPE_TBLK);
        if (null != mMApp && null != jsonMsg) {
            ArrayList<String> arrUsers = new ArrayList<String>();
            arrUsers.add(strUserId);
            for (int i=0;i<notifys.length;++i) {
                arrUsers.add(notifys[i]);
            }
            outMsgId = mMApp.SndMsgTo(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TBLK_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FMULTI_VALUE, arrUsers.toArray(new String[]{}), arrUsers.size());
        }
        return outMsgId;
    }

    public String MCNotifyForbidden(String strGroupId, String strUserId, int flag, String[] notifys) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndUid(strGroupId, strUserId, flag, JMClientType.MC_MSGTYPE_TFBD);
        if (null != mMApp && null != jsonMsg) {
            ArrayList<String> arrUsers = new ArrayList<String>();
            arrUsers.add(strUserId);
            for (int i=0;i<notifys.length;++i) {
                arrUsers.add(notifys[i]);
            }
            outMsgId = mMApp.SndMsgTo(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TFBD_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FMULTI_VALUE, arrUsers.toArray(new String[]{}), arrUsers.size());
        }
        return outMsgId;
    }

    public String MCNotifySettedMgr(String strGroupId, String strUserId, int flag, String[] notifys) {
        String outMsgId = null;
        String jsonMsg = MsMsgUtil.Encode2JsonWithGrpAndUid(strGroupId, strUserId, flag, JMClientType.MC_MSGTYPE_TMGR);
        if (null != mMApp && null != jsonMsg) {
            outMsgId = mMApp.SndMsg(strGroupId, "grpname", jsonMsg, EntityMsgType.EMsgTag.TCHAT_VALUE, EntityMsgType.EMsgType.TMGR_VALUE, CommonMsg.EModuleType.TLIVE_VALUE, CommonMsg.EMsgFlag.FGROUP_VALUE);
        }
        return outMsgId;
    }

    public int MCConnStatus() {
        if (null != mMApp) {
            return mMApp.ConnStatus();
        } else {
            return -10;
        }
    }

    public void MCSetUserId(String strUserId) {
        if (null != mMApp) {
            setmStrUserId(strUserId);
            mMApp.SetUserId(strUserId);
        }
    }

    public void MCSetToken(String strToken) {
        if (null != mMApp) {
            setmStrToken(strToken);
            mMApp.SetToken(strToken);
        }
    }

    public void MCSetNickName(String strNickName) {
        if (null != mMApp) {
            setmStrNname(strNickName);
            mMApp.SetNickName(strNickName);
        }
    }

    public void MCSetUIconUrl(String strUiconUrl) {
        if (null != mMApp) {
            setmStrUicon(strUiconUrl);
            mMApp.SetUIconUrl(strUiconUrl);
        }
    }


    //////////////////////////////////////////////////////////////
    //////////////////private method//////////////////////////////
    //////////////////////////////////////////////////////////////

    private void ProcessFetchResult() {
        if (IsFetchedAll()) {
            mIsFetched = true;
            mClientDelegate.OnMsgClientInitialized();
        } else {
            FetchAllSeqns();
        }
    }

    private boolean IsFetchedAll() {
        boolean yes = true;
        for (GroupInfo it : mGroupInfo) {
            String seqnId = it.getmSeqnId();
            int isfetched = it.getmIsFetched();
            System.out.println("IsFetchedAll seqnid:"+seqnId+", isfetched:"+isfetched);
            if (isfetched==0) {
                yes = false;
                break;
            } else {
                continue;
            }
        }
        return yes;
    }

    private void FetchAllSeqns() {
        for (GroupInfo it : mGroupInfo) {
            String seqnId = it.getmSeqnId();
            int isfetched = it.getmIsFetched();
            System.out.println("IsFetchedAll seqnid:"+seqnId+", isfetched:"+isfetched);
            if (isfetched==0) {
                if (seqnId.compareTo(mStrUserId)==0) {
                    mMApp.FetchSeqn();
                } else {
                    FetchGroupSeqn(seqnId);
                }
            }
        }
    }

    private void SyncAllSeqns() {
        for (GroupInfo it : mGroupInfo) {
            String seqnId = it.getmSeqnId();
            long seqn = it.getmSeqn();
            System.out.println("IsFetchedAll seqnid:" + seqnId + ", seqn:" + seqn);
                if (seqnId.compareTo(mStrUserId)==0) {
                    if (null != mMApp)
                        mMApp.SyncSeqn(seqn, CommonMsg.EMsgRole.RSENDER_VALUE);
                } else {
                    if (null != mMApp)
                        mMApp.SyncGroupSeqn(seqnId, seqn, CommonMsg.EMsgRole.RSENDER_VALUE);
                }
        }
    }

    private void FetchUserSeqn() {
        mMApp.FetchSeqn();
    }

    private void FetchGroupSeqn(String strGroupId) {
        mMApp.FetchGroupSeqn(strGroupId);
    }

    private void UpdateGroupInfoToDb(String strSeqnId, long seqn, int isFetched) {
        System.out.println("UpdateGroupInfoToDb seqn:"+strSeqnId+", isfetched:"+isFetched+", seqn:"+seqn);
        // update NSMutableArray
        for (int i=0;i<mGroupInfo.size();i++) {
            GroupInfo it = mGroupInfo.get(i);
            if (it.getmSeqnId().compareTo(strSeqnId)==0) {
                it.setmSeqn(seqn);
                it.setmIsFetched(isFetched);
            }
        }

        //GroupSeqnMap
        UpdateLocalSeqn(strSeqnId, seqn);
        // update Database
        mSqlite3Manager.UpdateGroupInfo(mStrUserId, strSeqnId, seqn, isFetched);
    }

    private void CheckUserOrInit(String strUid) {
        if (!mSqlite3Manager.IsUserExists(strUid, strUid)) {
            mSqlite3Manager.AddUser(strUid);
        }
    }

    private void GetLocalSeqnsFromDb() {
        if (mSqlite3Manager!=null) {
            ArrayList<GroupInfo> arr = mSqlite3Manager.GetGroupInfo(mStrUserId);
            for (GroupInfo in : arr) {
                String seqnId = in.getmSeqnId();
                long   seqn = in.getmSeqn();
                int    isfetched = in.getmIsFetched();
                System.out.println("get group info seqnid:"+seqnId+", isfetched:"+isfetched+", seqn:"+seqn);
                mGroupSeqn.put(seqnId, seqn);
                mGroupInfo.add(in);
            }
        }
    }

    private void PutLocalSeqnsToDb() {
        System.out.println("PubLocalSeqnsToDb was called");
        if (mSqlite3Manager!=null) {
            System.out.println("UpdateUserSeqn will be called...");
            for (String k  : mGroupSeqn.keySet())
            {
                mSqlite3Manager.UpdateGroupSeqn(mStrUserId, k, mGroupSeqn.get(k));
            }
        }
    }

    private void UpdateLocalSeqn(String strSeqnId, long seqn) {
        mReentrantLock.lock();
        mGroupSeqn.put(strSeqnId, seqn);
        mReentrantLock.unlock();
    }

    private void UpdateSeqnToDb(String strSeqnId, long seqn) {
        if (mSqlite3Manager!=null) {
            mSqlite3Manager.UpdateGroupSeqn(mStrUserId, strSeqnId, seqn);
        }
    }

    private void RemoveLocalSeqn(String strSeqnId) {
        mReentrantLock.lock();
        mGroupSeqn.remove(strSeqnId);
        mReentrantLock.unlock();
    }

    private long GetLocalSeqnFromId(String strSeqnId) {
        long lseqn = -1;
        mReentrantLock.lock();
        if (mGroupSeqn.containsKey(strSeqnId)) {
            lseqn = mGroupSeqn.get(strSeqnId);
        }
        mReentrantLock.unlock();
        return lseqn;
    }

    private void SyncSeqnFromDb2Core()
    {
        for (String k  : mGroupSeqn.keySet())
        {
            System.out.println("SyncSeqnFromDb2Core will call InitUserSeqns...");
            mMApp.InitUserSeqns(k, mGroupSeqn.get(k));
        }
    }

    private void UpdateSeqnFromDb2Core()
    {
        for (String k  : mGroupSeqn.keySet())
        {
            System.out.println("UpdateSeqnFromDb2Core will call UpdateUserSeqns...");
            mMApp.UpdateUserSeqns(k, mGroupSeqn.get(k));
        }
    }


    //////////////////////////////////////////////////////////////
    /////////////Implement JMClientHelper/////////////////////////
    //////////////////////////////////////////////////////////////

    @Override
    public void OnSndMsg(int code, String msgid) {
        System.out.println("MsgClient::OnSndMsg msgid:"+msgid+", code:"+code);
        if (null != mTxtMsgDelegate) {
            mTxtMsgDelegate.OnSendMessage(msgid, code);
        }
    }

    @Override
    public void OnCmdCallback(int code, int cmd, String groupid, JMSCbData data) {
        System.out.println("MsgClient::OnCmdCallback cmd:"+cmd+", groupid.length:"+groupid.length()+", result:"+data.type+", data.result:" + data.result + ", seqn:"+data.seqn+", data.data is:"+data.data);
        EnumMsgClient.EMSCmd ecmd = EnumMsgClient.EMSCmd.forNumber(cmd);
        switch (ecmd)
        {
            case MSADD_GROUP:
            {
                if (code == 0)
                {
                    System.out.println("OnCmdCallback add group ok, insert groupid and seqn, toggle callback");
                    if (null != mSqlite3Manager && null != mGroupDelegate) {
                        if (false == mSqlite3Manager.IsGroupExistsInDb(mStrUserId, groupid)) {
                            mSqlite3Manager.AddGroup(mStrUserId, groupid);
                        }
                        if (false == mSqlite3Manager.IsUserExists(mStrUserId, groupid)) {
                            mSqlite3Manager.AddGroupSeqn(mStrUserId, groupid, data.seqn);
                        }
                        UpdateLocalSeqn(groupid, data.seqn);
                        UpdateSeqnFromDb2Core();
                        mGroupDelegate.OnAddGroupSuccess(groupid);
                    }
                } else if (code == -1)
                {
                    if (null != mGroupDelegate) {
                        mGroupDelegate.OnAddGroupFailed(groupid, data.data, code);
                    }
                }
            }
            break;
            case MSRMV_GROUP:
            {
                if (code == 0)
                {
                    System.out.println("OnCmdCallback del group ok, del groupid and seqn, toggle callback");
                    if (null != mSqlite3Manager && null != mGroupDelegate) {
                        mSqlite3Manager.DelGroup(mStrUserId, groupid);
                        RemoveLocalSeqn(groupid);
                        // update seqn from db 2 core
                        mGroupDelegate.OnRmvGroupSuccess(groupid);
                    }
                } else if (code == -1)
                {
                    if (null != mGroupDelegate) {
                        mGroupDelegate.OnRmvGroupFailed(groupid, data.data, code);
                    }
                }
            }
            break;
            case MSFETCH_SEQN:
            {
                if (groupid.length()==0) // for user
                {
                    if (data.result==0)
                    {
                        UpdateGroupInfoToDb(mStrUserId, data.seqn, 1);
                    } else if (data.result==-1)
                    {
                        UpdateGroupInfoToDb(mStrUserId, data.seqn, -1);
                    } else if (data.result==-2)
                    {
                        UpdateGroupInfoToDb(mStrUserId, data.seqn, -2);
                    }
                } else { // for group
                    if (data.result==0)
                    {
                        UpdateGroupInfoToDb(groupid, data.seqn, 1);
                        UpdateSeqnFromDb2Core();
                    } else if (data.result==-1)
                    {
                        UpdateGroupInfoToDb(groupid, data.seqn, -1);
                    } else if (data.result==-2)
                    {
                        UpdateGroupInfoToDb(groupid, data.seqn, -2);
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void OnRecvMsg(long seqn, byte[] msg) {
        System.out.println("MsgClient::OnRecvMsg was called, seqn:" + seqn + ",msg.length:" + msg.length);
        UpdateLocalSeqn(mStrUserId, seqn);
        UpdateSeqnToDb(mStrUserId, seqn);

        Entity ee = null;
        try {
            ee = Entity.parseFrom(msg);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        if (null == ee) {
            System.out.println("OnRecvMsg parseFrom msg error, ee is null");
            return;
        }
        if (ee.getUsrFrom().compareTo(mStrUserId)==0) {
            System.out.println("MsgClient::OnRecvMsg recv the msg you just send!!!, so return\n");
            return;
        }
        System.out.println("OnRecvMsg EntityMsg.Entity msg tag:" + ee.getMsgTag() + ", cont:" + ee.getMsgCont() + ", romid:" + ee.getRomId() + ", usr_from:" + ee.getUsrFrom() + ", msgtype:" + ee.getMsgType());
        MSTxtMessage txtMsg = MsMsgUtil.DecodeJsonToMessage(ee.getMsgCont());
        if (null == txtMsg) {
            System.err.println("OnRecvMsg Decode Json To Message msg is null, error return");
            return;
        }
        txtMsg.setMillSec(ee.getMsgTime());

        if (null == mTxtMsgDelegate) {
            System.out.println("OnRecvMsg mTxtMsgDelegate is null!!!!!!");
            return;
        }

        switch (ee.getMsgType().getNumber())
        {
            case EntityMsgType.EMsgType.TTXT_VALUE:
            {
                mTxtMsgDelegate.OnRecvTxtMessage(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TFIL_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TPIC_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TAUD_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TVID_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TEMJ_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TSDF_VALUE:
            {
                mTxtMsgDelegate.OnRecvSelfDefMessage(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TLIV_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TREN_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TBLK_VALUE:
            {
                mTxtMsgDelegate.OnNotifyBlacklist(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TFBD_VALUE:
            {
                mTxtMsgDelegate.OnNotifyForbidden(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TMGR_VALUE:
            {
            }
                break;
            default:
            {
                System.out.println("MsgClient::OnRecvMsg ee.GetMsgType not handled:"+ee.getMsgType());
            }
            break;
        }
    }

    @Override
    public void OnRecvGroupMsg(long seqn, String seqnid, byte[] msg) {
        System.out.println("MsgClient::OnRecvGroupMsg was called seqn:" + seqn + ", seqnid:" + seqnid + ", msg.length:" + msg.length);
        UpdateLocalSeqn(seqnid, seqn);
        UpdateSeqnToDb(seqnid, seqn);

        Entity ee = null;
        try {
            ee = Entity.parseFrom(msg);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        if (null == ee) {
            System.out.println("OnRecvGroupMsg parseFrom msg error, ee is null");
            return;
        }
        if (ee.getUsrFrom().compareTo(mStrUserId)==0) {
            System.out.println("MsgClient::OnRecvMsg recv the msg you just send!!!, so return\n");
            return;
        }
        System.out.println("EntityMsg.Entity msg tag:" + ee.getMsgTag() + ", cont:" + ee.getMsgCont() + ", romid:" + ee.getRomId() + ", usr_from:" + ee.getUsrFrom() + ", msgtype:" + ee.getMsgType());
        MSTxtMessage txtMsg = MsMsgUtil.DecodeJsonToMessage(ee.getMsgCont());
        if (null == txtMsg) {
            System.err.println("OnRecvGroupMsg Decode Json To Message msg is null, error return");
            return;
        }
        txtMsg.setMillSec(ee.getMsgTime());

        if (null == mTxtMsgDelegate) {
            System.out.println("OnRecvGroupMsg mTxtMsgDelegate is null!!!!!!");
            return;
        }
        switch (ee.getMsgType().getNumber())
        {
            case EntityMsgType.EMsgType.TTXT_VALUE:
            {
                mTxtMsgDelegate.OnRecvTxtMessage(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TFIL_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TPIC_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TAUD_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TVID_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TEMJ_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TSDF_VALUE:
            {
                mTxtMsgDelegate.OnRecvSelfDefMessage(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TLIV_VALUE:
            {
                mTxtMsgDelegate.OnNotifyLive(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TREN_VALUE:
            {
                mTxtMsgDelegate.OnNotifyRedEnvelope(txtMsg);
            }
                break;
            case EntityMsgType.EMsgType.TBLK_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TFBD_VALUE:
            {
            }
                break;
            case EntityMsgType.EMsgType.TMGR_VALUE:
            {
                mTxtMsgDelegate.OnNotifySettedMgr(txtMsg);
            }
                break;
            default:
            {
                System.out.println("MsgClient::OnRecvGroupMsg ee.GetMsgType not handled:"+ee.getMsgType());
            }
            break;
        }
    }

    //////////////Below Used in this class//////////////////////////
    //////////////Later it will be removed//////////////////////////

    @Override
    public void OnSyncSeqn(long maxseqn, int role) {
        // if it is sender, this means client send msg, and just get the newmsg's seqn
        // if the new seqn is bigger 1 than cur seqn, it is ok, just update seqn.
        // if the new seqn is bigger 2 or 3 than cur seqn, this need sync data
        // if it is recver, this means client need sync data
        System.out.println("MsgClient::OnSyncSeqn NOT IMPLEMENT!!!");
    }

    @Override
    public void OnSyncGroupSeqn(String groupid, long maxseqn) {
        System.out.println("MsgClient::OnSyncGroupSeqn NOT IMPLEMENT!!!");
    }

    @Override
    public void OnGroupNotify(int code, String seqnid) {
        System.out.println("MsgClient::OnGroupNotify NOT IMPLEMENT!!!");
    }

    @Override
    public void OnNotifySeqn(int code, String seqnid) {
        System.out.println("MsgClient::OnNotifySeqn NOT IMPLEMENT!!!");
    }

    @Override
    public void OnNotifyData(int code, String seqnid) {
        System.out.println("MsgClient::OnNotifyData NOT IMPLEMENT!!!");
    }

    //////////////Above Used in this class//////////////////////////

    @Override
    public void OnMsgServerConnected() {
        System.out.println("OnMsgServerConnected was called, fetchallseqn once");
        FetchAllSeqns();
        if (null != mClientDelegate) {
            mClientDelegate.OnMsgServerConnected();
            mClientDelegate.OnMsgClientInitializing();
        }

        // check per second until get all fetch
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("MsgClient::OnMsgServerConnected timer was called...............");
                if (IsFetchedAll()) {
                    // stop timer here
                    mIsFetched = true;
                    UpdateSeqnFromDb2Core();
                    SyncAllSeqns();
                    if (null != mClientDelegate) {
                        mClientDelegate.OnMsgClientInitialized();
                    }
                    System.out.println("MsgClient::OnMsgServerConnected dispatch_source_cancel...ok");
                    this.cancel();
                } else {
                    FetchAllSeqns();
                }
            }
        }, 1000, 1000);
    }

    @Override
    public void OnMsgServerConnecting() {
        System.out.println("OnMsgServerConnecting was called");
        if (null != mClientDelegate) {
            mClientDelegate.OnMsgServerConnecting();
        }
    }

    @Override
    public void OnMsgServerDisconnect() {
        System.out.println("OnMsgServerDisconnect was called");
        if (null != mClientDelegate) {
            mClientDelegate.OnMsgServerDisconnect();
        }
    }

    @Override
    public void OnMsgServerConnectionFailure() {
        System.out.println("OnMsgServerConnectionFailer was called");
        if (null != mClientDelegate) {
            mClientDelegate.OnMsgServerConnectionFailure();
        }
    }
}
