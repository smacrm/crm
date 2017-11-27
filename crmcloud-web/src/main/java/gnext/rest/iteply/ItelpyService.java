package gnext.rest.iteply;

/**
 *
 * @author hungpham
 * @since Dec 20, 2016
 */
public interface ItelpyService {
    public interface ACT{
        public enum CheckUserExists{
            userID,             //ユーザID
        }; 
        
        public enum GetUserInfo{
            userID		//ユーザID
        }; 
        public enum GetSetting{
            
        };
        public enum GetCustomer{
            otherTelNumber	//顧客の電話番号
        }; 
        public enum GetHistoryList{
            recordNum,          //一度に取得する最大レコード数
            inOutFlag,          //1:着信　0:発信　指定無し:両方
            userID,             //ユーザID	省略した場合はすべてのオペレータ
            date                //取得するリストの年月日YYYY-MM-DD形式　省略した場合はすべての日が対象
        };
        public enum AddHistory{
            userID,		//ユーザID
            otherTelNumber,	//相手側の電話番号
            myTelNumber,        //	自分側の電話番号
            recordDate,         //録音時刻
            startTime,		//録音開始日時 （yyyy/MM/dd HH:mm:ss）
            inOutFlag,          //1：着信	0:発信
            issueCode,          //録音と紐付ける案件のID（省略可）
            mainRecovoiceID	//紐付ける主録音のID（省略可）
        };
        public enum FinishHistory{
            recovoiceID,	//録音ID（通話開始時のAddHistoryに対して返って来たID）
            finishTime,         //通話終了時刻 （yyyy/MM/dd HH:mm:ss）
            uniqueID		//CTI制御が発行する通話の識別ID
        };
        public enum Play{
            recovoiceID         //録音ID
        };
        public enum ViewDetail{
            issueCode           //通話履歴に紐付けられている案件ID
        };
        public enum StartItelpy{
            IssueCode,              //案件番号
            Act,                    //"StartItelpy",
            TelNumber,              //発信先電話番号欄にセット
            Code,                   //顧客コード
            PrefectureName,         //都道府県名
            Address1,               //住所1（市町村町名番地）
            Address2,               //住所2（建物名など）
            FirstNameKanji,         //名前漢字
            LastNameKanji,        //名字漢字
            FirstNameKana,          //名前仮名
            LastNameKana,         //名字仮名
            IssueReceiveLargeName,  //案件大分類名
            IssueReceiveMediumName, //案件中分類名
            IssueReceiveSmallName,  //案件小分類名
            IssueReceiveDetailName, //案件詳細名
            ClassName,              //顧客分類名
            Note,                   //備考
            ProductLargeName,       //商品大分類名
            ProductMediumName,      //商品中分類名
            ProductSmallName,       //商品小分類名
            ProductDetailName,      //商品詳細名
            SpecialtyFlag,          //特殊顧客かどうかのフラグ	0:特殊顧客ではない 1:特殊顧客
            SpeciallyFirstKanji,    //特殊顧客名前漢字
            SpeciallyLastKanji	,   //特殊顧客名字漢字
            SpeciallyFirstKana,     //特殊顧客名前カナ
            SpeciallyLastKana,    //特殊顧客名字カナ
            SpeciallyPrefectureName,//特殊顧客都道府県
            SpecialtyContents       //特殊顧客内容
        };
    }
}
