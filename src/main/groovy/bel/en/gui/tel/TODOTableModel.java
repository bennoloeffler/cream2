package bel.en.gui.tel;

import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamFirmaData;
import bel.en.data.CreamPersonData;
import bel.en.evernote.ENHelper;
import bel.en.helper.DateFinder;
import bel.en.localstore.SyncHandler;
import bel.learn._14_timingExecution.RunTimer;
import bel.util.RegexUtils;
import bel.util.StringSimilarity;
import com.evernote.edam.type.Note;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import javax.swing.table.AbstractTableModel;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static bel.en.evernote.ENHelper.findPersonDataLink;
import static bel.en.evernote.ENHelper.findTODOs;

/**
 * This extracts the todos from the model.
 * Special aspect: find the (N: Name) Pattern in the todo-string
 * and get tel, mobile and mail from
 */
@Log4j2 //did not work???
class TODOTableModel extends AbstractTableModel {

    @Setter
    String user = null;

    String[] columns = {"in...", "Zeit", "Tag", "Name", "Tel", "Mobil", "Email", "TODO"};

    List<CreamTodo> todos = new ArrayList<>();

    /**
     * the controller calls that, as soon as the underlying model tells about changes...
     * Then the table model creates new data and notifies the table.
     */
    public void update() {
        //SwingUtilities.invokeLater(() -> {
            todos = extractMyTodos();
            fireTableDataChanged();
        //});
    }


    @Override
    public int getRowCount() {
        return todos.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            CreamTodo todo = todos.get(rowIndex);
            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DateTimeFormatter.ofPattern("dd.MM.yy"));
            LocalDateTime todoToBeDone;
            if(todo.date == null && todo.time == null) {
                todoToBeDone = LocalDateTime.from(now);
            } else if(todo.date != null && todo.time == null) {
                todoToBeDone = LocalDateTime.from(todo.date.atStartOfDay());
            } else  if(todo.date == null && todo.time != null) {
                todoToBeDone = LocalDateTime.from(now.toLocalDate().atTime(todo.time));
            } else {
                todoToBeDone = LocalDateTime.of(todo.date, todo.time);
            }

            /*
            Duration d = Duration.between(now, todoToBeDone);
            System.out.println("---");
            System.out.println("d: " + d);
            long fullDays = d.getSeconds() / (long)(60*60*24);
            if(fullDays<0) {
                System.out.println("neg");
            }

            String sortBy;

          System.out.println("days: " + fullDays);
            long remainingSecs = Math.abs(d.getSeconds() % (long)(60*60*24));
            long fullHours = (remainingSecs) / (long)(60*60);
            System.out.println("hours: " + fullDays);
            remainingSecs = remainingSecs  % (60*60);
            long fullMinutes = remainingSecs / 60l;
            System.out.println("min: " + fullMinutes);
*/
            String sortBy = "0";
            Duration d = Duration.between(now, todoToBeDone);
            if(now.toLocalDate().equals(todo.getDate())) {
                long fullMinutes = d.toMinutes(); //d.getSeconds() % 60L;
                if (d.isNegative()) {
                    sortBy = "-0." + Math.abs(fullMinutes);
                } else {
                    sortBy = "0." + fullMinutes;
                }
            } else {
                d = Duration.between(now.toLocalDate().atStartOfDay(), todoToBeDone);
                long fullDays = d.getSeconds() / (long)(60*60*24);
                /*if (d.isNegative()) {
                    sortBy = "-" + fullDays;
                } else {*/
                    sortBy = "" + fullDays;
                //}
            }
            /*
            long fullMinutes = Math.abs(d.getSeconds() % (long)(60*60*24));

            String dStr;
            if(now.toLocalDate().equals(todo.getDate())) {
                dStr = "0." + + fullMinutes;
            } else {
                dStr = "" + fullDays + "d ";
            }
*/
            Double sortByDouble = Double.parseDouble(sortBy);

            switch(columnIndex) {
                case 0: return sortByDouble; //dStr;
                case 1: return todo.time == null ?  "---" : todo.time.format(DateTimeFormatter.ofPattern("HH:mm"));
                case 2: return todo.date == null ? "" + nowStr +" ?" : todo.date.format(DateTimeFormatter.ofPattern("dd.MM.yy"));
                case 3: return todo.name == null ? "---" : todo.getName();
                case 4: return todo.getTel();
                case 5: return todo.getHandy();
                case 6: return todo.getEmail();
                case 7: return todo.getTodo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * to get the row sort order for long (1st column) right
     * @param c
     * @return
     */
    @Override
    public Class getColumnClass(int c) {
        return c == 0 ? Double.class : String.class;
        //return getValueAt(0, c).getClass();
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }


    private List<CreamTodo> extractMyTodos() {
        RunTimer t = new RunTimer();
        //System.out.println("extractMyTodos()------------------------------------------------------------------------------------------");
        List<CreamTodo> result = new ArrayList<>();
        try {
            String userShortName = AbstractConfiguration.getConfig().getCurrentUser().getShortName();
            List<Note> allNotes = SyncHandler.get().getAllNotes();
            for(Note n: allNotes) {
                //System.out.println("in completeString: " + n.getTitle());
                //String pn = findFirstPhoneNr(n.getContent());
                if(isNormalNote(n)) {
                    //log.trace("found note: " + n.getTitle());

                    ArrayList<String> todos = findTODOs(n.getContent());
                    for (String todo : todos) {
                        if (todo.contains(userShortName + ":")) {
                            //log.trace("found: {}", n.getTitle());
                            //if(n.getTitle().contains("_Alle")) {
                            //    System.out.println("x");
                            //}
                            CreamTodo ct = new CreamTodo(n.getTitle(), n, todo);
                            Calendar c = new DateFinder(todo).get();
                            if(c != null) {
                                ct.setDate(LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)));
                            }
                            List<String> timeStr = RegexUtils.findWithRegex(todo, "\\d\\d?:\\d\\d?", 0);
                            if(timeStr.size()>0) {
                                DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                                        .append(DateTimeFormatter.ofPattern("" + "[HH:mm]" + "[H:mm]"));
                                DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
                                LocalTime lt = LocalTime.parse(timeStr.get(0), dateTimeFormatter);
                                ct.setTime(lt);
                            }

                            String personNameInTodo = findPersonDataLink(todo);
                            CreamFirmaData d = SyncHandler.get().getData(n);
                            if(personNameInTodo != null) {
                                //log.trace("found name-link in todo: {}", personNameInTodo);
                                //List<CreamPersonData> personDatas = d.persons.stream().filter(p->p.getAttr("Nachname").equals(person)).collect(toList());
                                if(d != null) {
                                    CreamPersonData personData = findBestHit(personNameInTodo, d.persons);
                                    if (personData != null) {
                                        //log.trace("found and using structured data for name-Link: {}", personData);
                                        ct.setName(personData.getAttr("Titel").value + " " + personData.getAttr("Vorname").value + " " + personData.getAttr("Nachname").value );
                                        ct.setHandy(personData.getAttr("Festnetz").value);
                                        ct.setTel(personData.getAttr("Mobil").value);
                                        ct.setEmail(personData.getAttr("Emails").value);
                                    } else {
                                        //log.trace("not found structured data. using name-Link: {}", personNameInTodo);
                                        ct.setName(personNameInTodo);
                                    }
                                    ct.setFirma(d);
                                }
                            } else {
                                // no linked structured data... use first phone nr, first mail and personNameInTodo
                                //log.trace("no name-Link. using phone and mail from raw content.");

                                val phone = ENHelper.findFirstPhoneNr(n.getContent());
                                if(phone != null) {
                                    ct.setHandy(phone + " ?");
                                    ct.setTel(phone + " ?");
                                } else {
                                    ct.setHandy("---");
                                    ct.setTel("---");
                                }
                                val mail = ENHelper.findFirstEmailAdress(n.getContent());
                                if(mail != null) {
                                    ct.setEmail(mail);
                                } else {
                                    ct.setEmail("---");
                                }
                                if(d != null) {
                                    ct.setFirma(d);
                                }
                            }
                            result.add(ct);
                        }
                    }
                }
            }
            //System.out.println("FINISHED: extractMyTodos()------------------------------------------------------------------------------------------");
            //t.stop("TODO: put into cache! TODOTableModel.extractMyTodos()");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public CreamPersonData findBestHit(String personNameInTodo, List<CreamPersonData> persons) {
        double best = 0;
        CreamPersonData mostSimilarPerson = null;
        for(CreamPersonData p: persons) {
            String vnStr = p.getAttr("Vorname").value;
            String nnStr = p.getAttr("Nachname").value;
            double vn = StringSimilarity.similarity(personNameInTodo, vnStr);
            double nn = StringSimilarity.similarity(personNameInTodo, nnStr);
            double vnPlusNn = StringSimilarity.similarity(personNameInTodo, vnStr + " " + nnStr);

            double localBest = Math.max(vn, Math.max(nn, vnPlusNn));
            if(localBest > best) {
                best = localBest;
                mostSimilarPerson = p;
            }
        }
        if(best>0.8) {
            return mostSimilarPerson;
        } else {
            return null;
        }
    }


    /**
     * duplicate!
     * @see bel.en.UebersichtAllUsers
     */
    private boolean isNormalNote(Note n) {
        if(n.getTitle().contains("UEBERSICHT")) return false;
        if(n.getTitle().contains("CRM-KONFIGURATION")) return false;
        if(n.getTitle().contains("ANGEBOTE_und_HOT")) return false;
        return true;
    }

    /*public Note getNote(int idx) {
        return todos.get(idx).n;
    }*/

    /*
    public CreamFirmaData getFirma(int idx) {
        return todos.get(idx).dataFile;
    }*/

    public CreamTodo getTODO(int idx) {
        return todos.get(idx);
    }

    public int indexOf(CreamTodo selectedTODO) {
        return todos.indexOf(selectedTODO);
    }

    //@Data
    public class CreamTodo {
        @Getter @Setter String name;
        @Getter @Setter String todo;
        @Getter @Setter String handy;
        @Getter @Setter String tel;
        @Getter @Setter LocalDate date;
        @Getter @Setter LocalTime time;
        @Getter @Setter String email;
        @Getter @Setter CreamFirmaData firma;
        @Getter @Setter Note note;
        public CreamTodo(@NonNull String name, @NonNull Note n, String todo) {
            this.name = name;
            this.note = n;
            this.todo = todo;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof CreamTodo)) return false;
            final CreamTodo other = (CreamTodo) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$name = this.name;
            final Object other$name = other.name;
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            final Object this$todo = this.todo;
            final Object other$todo = other.todo;
            if (this$todo == null ? other$todo != null : !this$todo.equals(other$todo)) return false;
            final Object this$handy = this.handy;
            final Object other$handy = other.handy;
            if (this$handy == null ? other$handy != null : !this$handy.equals(other$handy)) return false;
            final Object this$tel = this.tel;
            final Object other$tel = other.tel;
            if (this$tel == null ? other$tel != null : !this$tel.equals(other$tel)) return false;
            final Object this$date = this.date;
            final Object other$date = other.date;
            if (this$date == null ? other$date != null : !this$date.equals(other$date)) return false;
            final Object this$time = this.time;
            final Object other$time = other.time;
            if (this$time == null ? other$time != null : !this$time.equals(other$time)) return false;
            final Object this$email = this.email;
            final Object other$email = other.email;
            if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $name = this.name;
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            final Object $todo = this.todo;
            result = result * PRIME + ($todo == null ? 43 : $todo.hashCode());
            final Object $handy = this.handy;
            result = result * PRIME + ($handy == null ? 43 : $handy.hashCode());
            final Object $tel = this.tel;
            result = result * PRIME + ($tel == null ? 43 : $tel.hashCode());
            final Object $date = this.date;
            result = result * PRIME + ($date == null ? 43 : $date.hashCode());
            final Object $time = this.time;
            result = result * PRIME + ($time == null ? 43 : $time.hashCode());
            final Object $email = this.email;
            result = result * PRIME + ($email == null ? 43 : $email.hashCode());
            return result;
        }

        protected boolean canEqual(Object other) {
            return other instanceof CreamTodo;
        }
    }
}
