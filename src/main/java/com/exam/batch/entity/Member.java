package com.exam.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by seungwoo.song on 2022-08-17
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Member {

	private String memberId;
	private Long memberSeq;

}
