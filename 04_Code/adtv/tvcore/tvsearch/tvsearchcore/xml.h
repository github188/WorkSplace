/** 
 * \file xml.h
 * \brief XMLʵ�ʽ��������Ͷ����XML�ڵ������������.
 *
 * Copyright(c) 2007-2009 Novel-SuperTV, All rights reserved.
 *
 * Date        Author           Modification\n
 * ----------------------------------------------------------------\n
 * 2007-10-11  Fuwenchao        Created\n
 */
#ifndef NOVELSUPERTV_STBOS_XML_H
#define NOVELSUPERTV_STBOS_XML_H

#include "xmlparser.h"

#include <string>
#include <vector>
#include <stack>

namespace NovelSupertv
{
namespace stbruntime
{

/// XML�ڵ����Ͷ���
class  XmlNode
{
public:	
    /**
     * XML�ڵ㹹�캯��.
     *
     * \param[in] aname �ڵ�����.
     * \param[in] aproperties �ڵ�����.
     */
    XmlNode( std::string const& aname,
        std::vector<Property> const& aproperties  )
    : name( aname )
    , properties( aproperties )
    , parent_node_ptr( 0 )
    , first_child_node_ptr( 0 )
    , brother_node_ptr( 0 )
    {}

    /**
     *  ����������ȡ������ֵ.
     *
     * \param[in] name ��������.
     * \param[out] value ����ֵ.
     * 
     * \return �Ƿ�ȡ������ֵ
     * - true  ȡ������ֵ.
     * - false û��ȡ������ֵ.
     */
    bool property_value( std::string const& name, std::string& value );
    
    /**
     * ���ݽڵ����Ʋ��ҽڵ�.
     *
     * \param[in] name �ڵ�����.
     *
     * \return �ҵ��Ľڵ�ָ��.
     *
     * \note ���û�ҵ����ؿ�ָ��.
     */
    XmlNode* find_node( std::string const& name );

    std::string name;                ///<�ڵ�����
    std::string value;               ///<�ڵ�����
    std::vector<Property> properties;///<�ڵ�����
    XmlNode* parent_node_ptr;        ///<�ڵ㸸����
    XmlNode* first_child_node_ptr;   ///<�ڵ��һ�ӽڵ�
    XmlNode* brother_node_ptr;       ///<�ڵ��ֵܽڵ�
};

/// XMLʵ�ʽ���������,��XML��֯��һ���ڵ���.
class MyParser : public Parser
{
public:	
    /// ���������캯��.
    MyParser() : root_node_ptr_( 0 ) {}
    
    /// ��������������.
    ~MyParser();
    
    /**
     * ��ȡXml���ڵ�.
     *
     * \return ���ڵ�ָ��
     */
    XmlNode* root_node(){ return root_node_ptr_; }
    
    /**
     * �����Ƿ�ɹ�.
     *
     * \return �����Ƿ�ɹ�
     * -true  �����ɹ�.
     * -false ����ʧ��.
     */
    bool sucess() { return nodes_stack_.empty(); }
protected:
 /**
     * XML��ǩ��ʼ�¼�.
     *
     * \param[in] name ��ǩ��.
     * \param[in] propertys ��ǩ����ֵ�б�.
     *
     * \note �������<CHI type = "1">abc</CHI>,����������on_start��nameΪCHI
     *       propertysΪtype��1.
     */
    virtual void on_start( std::string const& name,
        std::vector<Property> const& propertys );
        
    /**
     * XML��ǩ�����¼�.
     *
     * \param[in] data ��ǩ����.
     * 
     * \note �������<CHI>abc</CHI>,����������on_data��dataΪabc.
     */    
    virtual void on_data( std::string const& data );
    
    /**
     * XML��ǩ�����¼�.
     *
     * \param[in] name ��ǩ��.
     *
     * \note �������<CHI>abc</CHI>,����������on_end��nameΪCHI.
     */
    virtual void on_end( std::string const& name );
private:
    void free_tree_node();
    XmlNode* root_node_ptr_;
    std::stack<XmlNode*> nodes_stack_;
};

/// �ڵ�����ṹ
class XmlNodeList
{
public:	
    /**
     * �ڵ������캯��
     *
     * \param[in] xml_node_ptr ������׽ڵ�.
     *
     * \note �����ϵĽڵ㶼���ֵܹ�ϵ
     * \par ʾ������:
     * \code
       MyParser parser;
       
       ParseXMLData( data, len, parser );
       if( parser.sucess() )
       {
           XmlNodeList list( parser.root_node->first_child_node_ptr );
           XmlNode* child_node = list.first();
           
           while( child_node )
           {
                //use child_node
                child_node = list.next();
           }
       }
       \endcode
     */
    XmlNodeList( XmlNode *xml_node_ptr );
    
    /**
     * ��ȡ�����һ���ڵ�.
     *
     * \return ����ĵ�һ�ڵ�
     */
    XmlNode* first();
    
    /**
     * ��ȡ����ǰ�ڵ�.
     *
     * \return ��ǰ�ڵ�
     */
    XmlNode* current();
    
    /**
     * ��ȡ������һ����.
     *
     * \return ��һ���ڵ�
     */
    XmlNode* next();
    
    /**
     * ��ȡ����ǰһ�ڵ�.
     *
     * \return ǰһ�ڵ�.
     */
    XmlNode* pre();
    
    /**
     * ��ȡ�������ڵ�.
     *
     * \return �������ڵ�.
     */
    XmlNode* last();
private:
    XmlNode* first_node_ptr_;
    XmlNode* last_node_ptr_;
    XmlNode* current_node_ptr_;
};

}
}


#endif

