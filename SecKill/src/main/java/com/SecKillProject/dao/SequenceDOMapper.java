package com.SecKillProject.dao;

import com.SecKillProject.dataobject.SequenceDO;
import org.springframework.stereotype.Component;

@Component
public interface SequenceDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Wed Aug 07 11:11:33 CST 2019
     */
    int deleteByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Wed Aug 07 11:11:33 CST 2019
     */
    int insert(SequenceDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Wed Aug 07 11:11:33 CST 2019
     */
    int insertSelective(SequenceDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Wed Aug 07 11:11:33 CST 2019
     */
    SequenceDO selectByPrimaryKey(String name);

    SequenceDO getSequenceByName(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Wed Aug 07 11:11:33 CST 2019
     */
    int updateByPrimaryKeySelective(SequenceDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Wed Aug 07 11:11:33 CST 2019
     */
    int updateByPrimaryKey(SequenceDO record);
}