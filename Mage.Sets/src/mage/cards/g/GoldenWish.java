/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.g;

import java.util.Set;
import java.util.UUID;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileSpellEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;

/**
 *
 * @author Plopman
 */
public class GoldenWish extends CardImpl {

    public GoldenWish(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{W}{W}");

        // You may choose an artifact or enchantment card you own from outside the game, reveal that card, and put it into your hand. Exile Golden Wish.
        this.getSpellAbility().addEffect(new GoldenWishEffect());
        this.getSpellAbility().addEffect(ExileSpellEffect.getInstance());
    }

    public GoldenWish(final GoldenWish card) {
        super(card);
    }

    @Override
    public GoldenWish copy() {
        return new GoldenWish(this);
    }
}

class GoldenWishEffect extends OneShotEffect {

    private static final String choiceText = "Choose an artifact or enchantment card you own from outside the game, and put it into your hand";

    private static final FilterCard filter = new FilterCard("artifact or enchantment card");

    static {
        filter.add(Predicates.or(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.ENCHANTMENT)));
    }

    public GoldenWishEffect() {
        super(Outcome.Benefit);
        this.staticText = "You may choose a artifact or enchantment card you own from outside the game, reveal that card, and put it into your hand";
    }

    public GoldenWishEffect(final GoldenWishEffect effect) {
        super(effect);
    }

    @Override
    public GoldenWishEffect copy() {
        return new GoldenWishEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller != null && sourceObject != null) {
            while (controller.chooseUse(Outcome.Benefit, choiceText, source, game)) {
                Cards cards = controller.getSideboard();
                if (cards.isEmpty()) {
                    game.informPlayer(controller, "You have no cards outside the game.");
                    break;
                }

                Set<Card> filtered = cards.getCards(filter, game);
                if (filtered.isEmpty()) {
                    game.informPlayer(controller, "You have no " + filter.getMessage() + " outside the game.");
                    break;
                }

                Cards filteredCards = new CardsImpl();
                filteredCards.addAll(filtered);

                TargetCard target = new TargetCard(Zone.OUTSIDE, filter);
                if (controller.choose(Outcome.Benefit, filteredCards, target, game)) {
                    Card card = controller.getSideboard().get(target.getFirstTarget(), game);
                    if (card != null) {
                        card.moveToZone(Zone.HAND, source.getSourceId(), game, false);
                        controller.revealCards(sourceObject.getIdName(), new CardsImpl(card), game);
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

}
