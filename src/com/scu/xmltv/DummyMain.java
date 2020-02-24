package com.scu.xmltv;

public class DummyMain
{

	public static void main(String[] args) throws Exception
	{
		XMLTVSourceCombiner sc = new XMLTVSourceCombiner(args[0], args[1]);
		sc.combineSource("episode-num");
		sc.writeUpdatedXMLTV(args[1] + ".upd");
	}

}
