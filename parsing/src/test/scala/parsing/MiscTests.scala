package parsing

import parsing.Parser.StringIn
import utest._

import scala.collection.mutable

object MiscTests extends TestSuite{

  val tests = TestSuite{
    'toString{
      def check(p: Parser[_], s: String) = {
        assert(p.toString == s.trim)
      }
      'Either {
        check("A" | "B", """("A" | "B")""")
        check("A" | "B" | "C", """("A" | "B" | "C")""")
        check(("A" | "B") | "C", """("A" | "B" | "C")""")
        check("A" | ("B" | "C"), """("A" | "B" | "C")""")
      }
      'Sequence {
        check("A" ~ "BBB", """("A" ~ "BBB")""")
        check("A" ~ "B" ~ "C", """("A" ~ "B" ~ "C")""")
        check(("A" ~ "B") ~ "C", """("A" ~ "B" ~ "C")""")
        check("A" ~ ("B" ~ "C"), """("A" ~ "B" ~ "C")""")
      }
      'Mixed{
        check(("A" ~ "B") | "C", """(("A" ~ "B") | "C")""")
        check("A" ~ ("B" | "C"), """("A" ~ ("B" | "C"))""")
        check(("A" | "B") ~ "C", """(("A" | "B") ~ "C")""")
        check("A" | ("B" ~ "C"), """("A" | ("B" ~ "C"))""")
      }
      'rep{
        check("A".rep, """ "A".rep """)
        check(("A" | "B").rep, """ ("A" | "B").rep """)
        check(("A".? | "B").rep, """ ("A".? | "B").rep """)
        check(("A".? | "B").rep1, """ ("A".? | "B").rep1 """)
        check(("A".? | "B").rep("C"), """ ("A".? | "B").rep("C") """)
        check(("A".? | "B").rep1("C" ~ "D" | "E"), """ ("A".? | "B").rep1((("C" ~ "D") | "E")) """)
      }
      'lookahead{
        check(&("A") ~ "ABC", """(&("A") ~ "ABC")""")
        check(!"A" ~ "ABC", """(!("A") ~ "ABC")""")
        check("A".! ~ "ABC".!, """("A".! ~ "ABC".!)""")
      }
      'named{
        val Foo = R( "A" )
        check(Foo, """Foo""")
        check(End, """End""")
        check(Start, """Start""")
        check(Pass, """Pass""")
        check(Fail, """Fail""")
        check(AnyChar, """AnyChar""")
        check(CharIn("abc", "d", Seq('1', '2', '3')), """CharIn("abcd123")""")
        check(
          StringIn("mango", "mandarin", "mangosteen"),
          """StringIn("mango", "mandarin", "mangosteen")"""
        )
        check(CharPred(_.isUpper), """CharPred(<function1>)""")
      }
    }
    'logging{
      val logged = mutable.Buffer.empty[String]
      val Foo = R( "A".log("A", logged +=) ~ "B".!.log("B", logged +=) ).log("AB", logged+=)
      Foo.parse("AB")
      val expected = Seq(
        "+AB:0",
        "  +A:0",
        "  -A:0:Success((),1,false)",
        "  +B:1",
        "  -B:1:Success(B,2,false)",
        "-AB:0:Success(B,2,false)"
      )
      assert(logged == expected)
    }
  }
}
