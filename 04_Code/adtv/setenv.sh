#!/bin/bash
# �ף� �汾˵����
# Ŀǰ����� novel(default), fuzhou, yongan, wuzhou, huanggang ������汾��
# ����ǰҪ���û���������
# ���� . setenv.sh fuzhou
#      . setenv.sh ���Ӳ�������ʾ�����Ͳ鿴��ǰ���õı�����
#      Ĭ���ǲ����κα�������Ӧ�����汾��

# �ң���������˵��
# JS_USE_SHUMACA_SEARCH ��ָ��TVSEARCH ʱ����shuma ��systemID
# JS_USE_SHUMA_CALIB_TEST ��ָ����libstbca.so ʱ�� Ҫ���������calib ���Կ�
# 
# 2. JS_SHUMA_CALIB_1102, ��ʹ��union shuma calib, ���Ҹ�calib Ҫ���� 1102 table-extension.����ƸԵ�����
# 3. JS_FUZHOU_NIT_SEARCH. ��ָ���fuzhou ������nit ����
# 4. ����µ�������: 
#   a. ��ClearVar ������±���
#   b. ��MyHelp �и���ʹ��˵��
#   c. ��ShowEnv �и�����ʾ
#   d. ��elif then �и�����������
# any question, please contact hjj

function ClearVar()
{

	unset JS_BEJING_NIT_SEARCH
	unset JS_FUZHOU_NIT_SEARCH
	unset JS_QINGDAO_NIT_SEARCH

	unset JS_USE_NOVELCA_SEARCH
	unset JS_USE_SHUMACA_SEARCH
	unset JS_USE_GEHUACA_SEARCH

	unset JS_USE_NOVEL_CALIB

	unset JS_USE_SHUMA_CALIB_NORMAL
	unset JS_USE_SHUMA_CALIB_TEST
	unset JS_USE_SHUMA_CALIB_HUANGGANG
	unset JS_USE_SHUMA_CALIB_YONGAN
	unset JS_USE_SHUMA_CALIB_NANPING
	unset JS_USE_SHUMA_CALIB_NINGDE
	unset JS_USE_SHUMA_CALIB_LONGYAN
	unset JS_USE_SHUMA_CALIB_PUTIAN

	unset JS_USE_UNION_CALIB
	unset JS_UNION_SHUMA_CALIB 
	unset JS_SHUMA_CALIB_1102
	unset JS_SHUMA_CALIB_1103
	unset JS_UNION_GEHUA_CALIB 
}
function MyHelp()
{
	echo '*****************************';
	echo "you can set enviroment!"
	echo '*****************************';
	echo "EX: . setenv  novel"
	echo "EX: . setenv  fuzhou"
	echo "EX: . setenv  qingdao"
	echo "------------------------------"
	echo "EX: . setenv  testgehua"
	echo "------------------------------"
	echo "EX: . setenv  testshuma"
	echo "EX: . setenv  huanggang"
	echo "EX: . setenv  nanping"
	echo "EX: . setenv  yongan"
	echo "EX: . setenv  wuzhou"
	echo "EX: . setenv  ningde"
	echo "EX: . setenv  longyan"
	echo "EX: . setenv  putian"
	echo '*****************************';
}

function ShowEnv()
{
	echo '//////////////////////////////////';
	echo 'now enviroment'
	echo '//////////////////////////////////';
	echo '$JS_BEJING_NIT_SEARCH =' $JS_BEJING_NIT_SEARCH
	echo '$JS_FUZHOU_NIT_SEARCH =' $JS_FUZHOU_NIT_SEARCH
	echo '$JS_QINGDAO_NIT_SEARCH =' $JS_QINGDAO_NIT_SEARCH
	echo '//////////////////////////////////';
	echo '$JS_USE_NOVELCA_SEARCH =' $JS_USE_NOVELCA_SEARCH
	echo '$JS_USE_SHUMACA_SEARCH =' $JS_USE_SHUMACA_SEARCH
	echo '$JS_USE_GEHUACA_SEARCH =' $JS_USE_GEHUACA_SEARCH
	echo '//////////////////////////////////';
	echo '$JS_USE_NOVEL_CALIB =' $JS_USE_NOVEL_CALIB
	echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	echo '$JS_USE_SHUMA_CALIB_NORMAL=' $JS_USE_SHUMA_CALIB_NORMAL
	echo '$JS_USE_SHUMA_CALIB_TEST =' $JS_USE_SHUMA_CALIB_TEST
	echo '$JS_USE_SHUMA_CALIB_HUANGGANG =' $JS_USE_SHUMA_CALIB_HUANGGANG
	echo '$JS_USE_SHUMA_CALIB_YONGAN =' $JS_USE_SHUMA_CALIB_YONGAN
	echo '$JS_USE_SHUMA_CALIB_NANPING =' $JS_USE_SHUMA_CALIB_NANPING
	echo '$JS_USE_SHUMA_CALIB_NINGDE =' $JS_USE_SHUMA_CALIB_NINGDE
	echo '$JS_USE_SHUMA_CALIB_LONGYAN =' $JS_USE_SHUMA_CALIB_LONGYAN
	echo '$JS_USE_SHUMA_CALIB_PUTIAN =' $JS_USE_SHUMA_CALIB_PUTIAN
	echo "~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	echo '$JS_USE_UNION_CALIB =' $JS_USE_UNION_CALIB
	echo '$JS_UNION_GEHUA_CALIB =' $JS_UNION_GEHUA_CALIB 
	echo '$JS_UNION_SHUMA_CALIB =' $JS_UNION_SHUMA_CALIB 
	echo '$JS_SHUMA_CALIB_1102=' $JS_SHUMA_CALIB_1102
	echo '$JS_SHUMA_CALIB_1103=' $JS_SHUMA_CALIB_1103
	echo '//////////////////////////////////';
}

if [ "$1" = "novel" ] 
then
	ClearVar
	export JS_BEJING_NIT_SEARCH=1
	export JS_USE_NOVELCA_SEARCH=1
	export JS_USE_NOVEL_CALIB=1

elif [ "$1" = "fuzhou" ] 
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_NOVELCA_SEARCH=1
	export JS_USE_NOVEL_CALIB=1

elif [ "$1" = "yongan" ] || [ "$1" = "wuzhou" ]
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_NORMAL=1
	export JS_USE_SHUMA_CALIB_YONGAN=1

elif [ "$1" = "nanping" ] 
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_NORMAL=1
	export JS_USE_SHUMA_CALIB_NANPING=1

elif [ "$1" = "huanggang" ] 
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_HUANGGANG=1
	export JS_USE_SHUMA_CALIB_NORMAL=1

#elif [ "$1" = "ningde" ] 
#then
#	ClearVar
#	export JS_FUZHOU_NIT_SEARCH=1
#	export JS_USE_SHUMACA_SEARCH=1
#	export JS_USE_UNION_CALIB=1
#	export JS_UNION_SHUMA_CALIB=1
#	export JS_SHUMA_CALIB_1103=1

elif [ "$1" = "ningde" ] 
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_NINGDE=1
	export JS_USE_SHUMA_CALIB_NORMAL=1

#elif [ "$1" = "putian" ]
#then
#	ClearVar
#	export JS_FUZHOU_NIT_SEARCH=1
#	export JS_USE_SHUMACA_SEARCH=1
#	export JS_USE_UNION_CALIB=1
#	export JS_UNION_SHUMA_CALIB=1
#	export JS_SHUMA_CALIB_1102=1
#

elif [ "$1" = "putian" ]
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_NORMAL=1
	export JS_USE_SHUMA_CALIB_PUTIAN=1

elif [ "$1" = "longyan" ]
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_NORMAL=1
	export JS_USE_SHUMA_CALIB_LONGYAN=1


elif [ "$1" = "testshuma" ]
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_SHUMA_CALIB_TEST=1
	export JS_USE_SHUMA_CALIB_NORMAL=1


elif [ "$1" = "testgehua" ] 
then
	ClearVar
	export JS_BEJING_NIT_SEARCH=1
	export JS_USE_GEHUACA_SEARCH=1
	export JS_USE_UNION_CALIB=1
	export JS_UNION_GEHUA_CALIB=1

elif [ "$1" = "qingdao" ]
then
	ClearVar
	export JS_QINGDAO_NIT_SEARCH=1
	export JS_USE_NOVELCA_SEARCH=1
	export JS_USE_NOVEL_CALIB=1

elif [ "$1" = "test" ] 
then
	ClearVar
	export JS_FUZHOU_NIT_SEARCH=1
	export JS_USE_SHUMACA_SEARCH=1
	export JS_USE_UNION_CALIB=1
	export JS_UNION_SHUMA_CALIB=1


else
	MyHelp
fi
ShowEnv
