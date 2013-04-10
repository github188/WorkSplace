#include "xml.h"

namespace NovelSupertv
{
namespace stbruntime
{

bool XmlNode::property_value( std::string const& name,
    std::string& value )
{
    std::vector<Property>::iterator it = properties.begin();
    std::vector<Property>::iterator end = properties.end();
    for( ; it != end; ++it )
    {
        if( name == it->name )
        {
            value = it->value;
            return true;
        }
    }
    return false;
}

XmlNode* XmlNode::find_node( std::string const& name )
{
    if( name == this->name )
        return this;
    else
    {
        XmlNode* pNode = 0;
        if( first_child_node_ptr )
            pNode = first_child_node_ptr->find_node( name );
        if( pNode )
            return pNode;
         else if( brother_node_ptr )
            return brother_node_ptr->find_node( name );
        return pNode;
    }
}

XmlNodeList::XmlNodeList( XmlNode* xml_node_ptr )
{
    XmlNode *node_ptr = xml_node_ptr;

    first_node_ptr_ = xml_node_ptr;
    current_node_ptr_ = xml_node_ptr;
    while( node_ptr )
    {
        if( node_ptr->brother_node_ptr )
            node_ptr = node_ptr->brother_node_ptr;
        else
        {
            last_node_ptr_ = node_ptr;
            break;
        }
    }
}

XmlNode* XmlNodeList::first()
{
    current_node_ptr_ = first_node_ptr_;
    return first_node_ptr_;
}

XmlNode* XmlNodeList:: current()
{
    return current_node_ptr_;
}

XmlNode* XmlNodeList::next()
{
    XmlNode* node_ptr = current_node_ptr_;
    if( node_ptr != last_node_ptr_ )
    {
        current_node_ptr_ = node_ptr->brother_node_ptr;
        return current_node_ptr_;
    }
    return 0;
}

XmlNode* XmlNodeList::pre()
{
    XmlNode *node_ptr = current_node_ptr_;

    if( node_ptr != first_node_ptr_ )
    {
        current_node_ptr_ = node_ptr->parent_node_ptr;
        return current_node_ptr_;
    }
    return 0;
}

XmlNode* XmlNodeList::last()
{
    current_node_ptr_ = last_node_ptr_;
    return last_node_ptr_;
}

void delete_xml_node( XmlNode *node_ptr )
{
    if( !node_ptr )//空树
        return;
    else
    {//树不为空
        //删除第一个孩子节点
        delete_xml_node( node_ptr->first_child_node_ptr );
        //删除兄弟节点
        delete_xml_node( node_ptr->brother_node_ptr );
        //删除该节点
        delete node_ptr;
    }
}

void add_brother_node( XmlNode* first_child_node_ptr,
    XmlNode* brother_node_ptr )
{
    XmlNode *node_ptr = first_child_node_ptr;
    while( true )
    {
        if ( !node_ptr->brother_node_ptr )//没有兄弟，增加兄弟节点
        {
            node_ptr->brother_node_ptr = brother_node_ptr;
            brother_node_ptr->parent_node_ptr = node_ptr;
            break;
        }
        else//取得的下一个兄弟节点
        {
            node_ptr = node_ptr->brother_node_ptr;
        }
    }
}

MyParser::~MyParser()
{
    free_tree_node();
}

void MyParser::free_tree_node()
{
    if( root_node_ptr_ )
    {
        delete_xml_node( root_node_ptr_ );
        root_node_ptr_ = 0;
    }
}

void MyParser::on_start( std::string const& name,
    std::vector<Property> const& propertys )
{
    if( nodes_stack_.empty() )
    {//根节点
        free_tree_node();
        root_node_ptr_ = new XmlNode( name, propertys );
        nodes_stack_.push(root_node_ptr_);
    }
    else
    {
       XmlNode* parent_node_ptr = nodes_stack_.top();
       XmlNode* node_ptr = new XmlNode( name, propertys );

       if( parent_node_ptr->first_child_node_ptr )
            add_brother_node( parent_node_ptr->first_child_node_ptr, node_ptr );
       else
       {
            parent_node_ptr->first_child_node_ptr = node_ptr;
            node_ptr->parent_node_ptr = parent_node_ptr;
       }
       nodes_stack_.push( node_ptr );
    }
}

void MyParser::on_data( std::string const& data )
{
    XmlNode* node_ptr = nodes_stack_.top();
    node_ptr->value = data;
}

void MyParser::on_end( std::string const& name )
{
    nodes_stack_.pop();
}

}
}
