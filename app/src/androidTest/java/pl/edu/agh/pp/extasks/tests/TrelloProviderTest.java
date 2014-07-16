package pl.edu.agh.pp.extasks.tests;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.trello4j.Trello;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;
import org.trello4j.model.Member;

import java.util.LinkedList;
import java.util.List;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.NoteList;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;

import static org.mockito.Mockito.when;

/**
 * Class testing TrelloProvider.class with Trello from Trello4J mocked.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class TrelloProviderTest extends TestCase {
    @Mock
    private Trello trelloManagerMock;
    private TrelloProvider trelloProvider;
    private String key;
    private String token;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        trelloProvider = new TrelloProvider();
        key = "key";
        token = "token";
    }

    /**
     * Tests consistency and correctness of recieved Notes via TrelloProvider.
     */
    @SmallTest
    public void testGetNotesFromService() {
        trelloProvider.setKey(key);
        trelloProvider.setToken(token);
        trelloProvider.setTrelloManager(trelloManagerMock);
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        when(trelloManagerMock.getMemberByToken(token)).thenReturn(member);
        List<Board> list = new LinkedList<Board>();
        Board board = new Board();
        board.setId("ID1");
        board.setName("Name1");
        list.add(board);
        board = new Board();
        board.setId("ID2");
        board.setName("Name2");
        list.add(board);
        when(trelloManagerMock.getBoardsByMember(memberId)).thenReturn(list);
        List<Card> cardList1 = new LinkedList<Card>();
        List<Card> cardList2 = new LinkedList<Card>();
        Card card1 = new Card();
        card1.setId("IDCard1");
        card1.setName("NameCard1");
        cardList1.add(card1);
        Card card2 = new Card();
        card2.setId("IDCard2");
        card2.setName("NameCard2");
        cardList2.add(card2);
        when(trelloManagerMock.getCardsByBoard("ID1")).thenReturn(cardList1);
        when(trelloManagerMock.getCardsByBoard("ID2")).thenReturn(cardList2);
        Checklist checklist1 = new Checklist();
        List<Checklist> checklist1List = new LinkedList<Checklist>();
        when(trelloManagerMock.getChecklistByCard("IDCard1")).thenReturn(checklist1List);
        checklist1.setId("IDChecklist1");
        checklist1.setName("Checklist1");
        checklist1List.add(checklist1);
        Checklist checklist2 = new Checklist();
        List<Checklist> checklist2List = new LinkedList<Checklist>();
        when(trelloManagerMock.getChecklistByCard("IDCard2")).thenReturn(checklist2List);
        checklist2.setId("IDChecklist2");
        checklist2.setName("Checklist2");
        checklist2List.add(checklist2);
        List<Checklist.CheckItem> checkItemList1 = new LinkedList<Checklist.CheckItem>();
        Checklist.CheckItem item = checklist1.new CheckItem();
        item.setId("IDItem11");
        item.setName("Item11");
        checkItemList1.add(item);
        item = checklist1.new CheckItem();
        item.setId("IDItem12");
        item.setName("Item12");
        checkItemList1.add(item);
        List<Checklist.CheckItem> checkItemList2 = new LinkedList<Checklist.CheckItem>();
        item = checklist2.new CheckItem();
        item.setId("IDItem21");
        item.setName("Item21");
        checkItemList2.add(item);
        when(trelloManagerMock.getCheckItemsByChecklist("IDChecklist1")).thenReturn(checkItemList1);
        when(trelloManagerMock.getCheckItemsByChecklist("IDChecklist2")).thenReturn(checkItemList2);
        trelloProvider.getNotesFromService();
        List<NoteList> noteLists = trelloProvider.getNotes();
        assertEquals(2, noteLists.size());
        NoteList firstNoteList = noteLists.get(0);
        assertEquals(2, firstNoteList.size());
        assertEquals("NameCard1", firstNoteList.getName());
        NoteList secondNoteList = noteLists.get(1);
        assertEquals(1, secondNoteList.size());
        assertEquals("NameCard2", secondNoteList.getName());
        List<Note> firstNotes = firstNoteList.getNotes();
        List<Note> secondNotes = secondNoteList.getNotes();
        assertEquals("Item11", firstNotes.get(0).getText());
        assertEquals("Item12", firstNotes.get(1).getText());
        assertEquals("Item21", secondNotes.get(0).getText());

    }



}
