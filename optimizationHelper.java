
/**
* optimizationHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from generator.idl
* Tuesday, December 18, 2018 9:44:01 PM CET
*/

abstract public class optimizationHelper
{
  private static String  _id = "IDL:optimization:1.0";

  public static void insert (org.omg.CORBA.Any a, optimization that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static optimization extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (optimizationHelper.id (), "optimization");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static optimization read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_optimizationStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, optimization value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static optimization narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof optimization)
      return (optimization)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _optimizationStub stub = new _optimizationStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static optimization unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof optimization)
      return (optimization)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _optimizationStub stub = new _optimizationStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
