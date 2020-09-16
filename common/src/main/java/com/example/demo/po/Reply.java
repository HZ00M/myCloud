package com.example.demo.po;

import com.example.core.sqlgen.annotation.Id;
import com.example.core.sqlgen.annotation.TableField;
import com.example.core.sqlgen.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("hc_question_reply_record")
@Data
public class Reply {
    @Id
    private Integer id;
    @TableField("reply_text")
    private String text;
    @TableField("reply_wheel_time")
    private Date replyTime;
    @TableField("ctime")
    private Date createTime;
    @TableField("mtime")
    private Date updateTime;
}
