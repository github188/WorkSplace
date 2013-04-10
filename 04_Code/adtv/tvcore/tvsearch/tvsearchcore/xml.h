/** 
 * \file xml.h
 * \brief XML实际解析器类型定义和XML节点操作函数定义.
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

/// XML节点类型定义
class  XmlNode
{
public:	
    /**
     * XML节点构造函数.
     *
     * \param[in] aname 节点名称.
     * \param[in] aproperties 节点属性.
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
     *  根据属性名取得属性值.
     *
     * \param[in] name 属性名称.
     * \param[out] value 属性值.
     * 
     * \return 是否取到属性值
     * - true  取到属性值.
     * - false 没有取到属性值.
     */
    bool property_value( std::string const& name, std::string& value );
    
    /**
     * 根据节点名称查找节点.
     *
     * \param[in] name 节点名称.
     *
     * \return 找到的节点指针.
     *
     * \note 如果没找到返回空指针.
     */
    XmlNode* find_node( std::string const& name );

    std::string name;                ///<节点名称
    std::string value;               ///<节点内容
    std::vector<Property> properties;///<节点属性
    XmlNode* parent_node_ptr;        ///<节点父窗口
    XmlNode* first_child_node_ptr;   ///<节点第一子节点
    XmlNode* brother_node_ptr;       ///<节点兄弟节点
};

/// XML实际解析器类型,将XML组织成一个节点树.
class MyParser : public Parser
{
public:	
    /// 解析器构造函数.
    MyParser() : root_node_ptr_( 0 ) {}
    
    /// 解析器析构函数.
    ~MyParser();
    
    /**
     * 获取Xml根节点.
     *
     * \return 根节点指针
     */
    XmlNode* root_node(){ return root_node_ptr_; }
    
    /**
     * 解析是否成功.
     *
     * \return 解析是否成功
     * -true  解析成功.
     * -false 解析失败.
     */
    bool sucess() { return nodes_stack_.empty(); }
protected:
 /**
     * XML标签开始事件.
     *
     * \param[in] name 标签名.
     * \param[in] propertys 标签属性值列表.
     *
     * \note 例如对于<CHI type = "1">abc</CHI>,解析器调用on_start是name为CHI
     *       propertys为type和1.
     */
    virtual void on_start( std::string const& name,
        std::vector<Property> const& propertys );
        
    /**
     * XML标签内容事件.
     *
     * \param[in] data 标签内容.
     * 
     * \note 例如对于<CHI>abc</CHI>,解析器调用on_data是data为abc.
     */    
    virtual void on_data( std::string const& data );
    
    /**
     * XML标签结束事件.
     *
     * \param[in] name 标签名.
     *
     * \note 例如对于<CHI>abc</CHI>,解析器调用on_end是name为CHI.
     */
    virtual void on_end( std::string const& name );
private:
    void free_tree_node();
    XmlNode* root_node_ptr_;
    std::stack<XmlNode*> nodes_stack_;
};

/// 节点链表结构
class XmlNodeList
{
public:	
    /**
     * 节点链表构造函数
     *
     * \param[in] xml_node_ptr 链表的首节点.
     *
     * \note 链表上的节点都是兄弟关系
     * \par 示例代码:
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
     * 获取链表第一个节点.
     *
     * \return 链表的第一节点
     */
    XmlNode* first();
    
    /**
     * 获取链表当前节点.
     *
     * \return 当前节点
     */
    XmlNode* current();
    
    /**
     * 获取链表下一个节.
     *
     * \return 下一个节点
     */
    XmlNode* next();
    
    /**
     * 获取链表前一节点.
     *
     * \return 前一节点.
     */
    XmlNode* pre();
    
    /**
     * 获取链表最后节点.
     *
     * \return 链表最后节点.
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

